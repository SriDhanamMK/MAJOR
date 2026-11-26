#!/usr/bin/env python3
"""
Tree-sitter Parser Engine for C, C++, and Python.
Provides genuine AST extraction and syntax error detection.
Used by the Spring Boot backend via safe standard I/O communication.
"""

import sys
import json
import traceback

try:
    from tree_sitter import Language, Parser
    import tree_sitter_c as tsc
    import tree_sitter_cpp as tscpp
    import tree_sitter_python as tspy
    
    LANGUAGES = {
        "C": Language(tsc.language()),
        "CPP": Language(tscpp.language()),
        "PYTHON": Language(tspy.language())
    }
    
    PARSERS = {
        "C": Parser(LANGUAGES["C"]),
        "CPP": Parser(LANGUAGES["CPP"]),
        "PYTHON": Parser(LANGUAGES["PYTHON"])
    }
    INITIALIZED = True
    INIT_ERROR = None
except Exception as e:
    INITIALIZED = False
    INIT_ERROR = str(e)
    LANGUAGES = {}
    PARSERS = {}

def get_health():
    return {
        "initialized": INITIALIZED,
        "error": INIT_ERROR,
        "languages": {
            "C": "C" in PARSERS,
            "CPP": "CPP" in PARSERS,
            "PYTHON": "PYTHON" in PARSERS
        },
        "versions": {
            "tree_sitter": "0.26.0",
            "tree_sitter_c": "0.24.2",
            "tree_sitter_cpp": "0.23.4",
            "tree_sitter_python": "0.25.0"
        }
    }

def collect_diagnostics(node, source_lines):
    diagnostics = []
    
    def walk(n):
        if n.is_missing:
            line = n.start_point[0] + 1
            col = n.start_point[1]
            diagnostics.append({
                "severity": "ERROR",
                "line": line,
                "column": col,
                "endLine": n.end_point[0] + 1,
                "endColumn": n.end_point[1],
                "message": f"Missing expected token '{n.type}' at line {line}, col {col}"
            })
        elif n.type == "ERROR":
            line = n.start_point[0] + 1
            col = n.start_point[1]
            snippet = ""
            try:
                snippet = n.text.decode('utf-8', errors='replace').strip()
                if len(snippet) > 40:
                    snippet = snippet[:40] + "..."
            except Exception:
                pass
            msg = f"Syntax error at line {line}, col {col}"
            if snippet:
                msg += f": unexpected construct '{snippet}'"
            diagnostics.append({
                "severity": "ERROR",
                "line": line,
                "column": col,
                "endLine": n.end_point[0] + 1,
                "endColumn": n.end_point[1],
                "message": msg
            })
        for child in n.children:
            walk(child)
            
    walk(node)
    return diagnostics

def serialize_ast(node, source_bytes, node_counter, max_depth=100, current_depth=0):
    node_id = f"n{node_counter[0]}"
    node_counter[0] += 1
    
    # Only keep reasonable length text representation for leaf or small nodes
    is_leaf = len(node.children) == 0
    raw_text = node.text.decode('utf-8', errors='replace') if is_leaf or len(node.text) <= 80 else None
    
    # Tree-sitter captures field names in many grammars (e.g., condition, body, declarator)
    # We can serialize named and structural children
    children = []
    if current_depth < max_depth:
        for child in node.children:
            # We preserve all named nodes, plus significant anonymous punctuation/keywords if needed
            children.append(serialize_ast(child, source_bytes, node_counter, max_depth, current_depth + 1))
            
    return {
        "id": node_id,
        "type": node.type,
        "isNamed": node.is_named,
        "isMissing": node.is_missing,
        "isError": node.type == "ERROR",
        "hasError": node.has_error,
        "startLine": node.start_point[0] + 1,
        "startCol": node.start_point[1],
        "endLine": node.end_point[0] + 1,
        "endCol": node.end_point[1],
        "startByte": node.start_byte,
        "endByte": node.end_byte,
        "text": raw_text,
        "children": children
    }

def parse_code(language_str, source_code):
    if not INITIALIZED:
        return {
            "status": "ERROR",
            "message": f"Tree-sitter parser failed to initialize: {INIT_ERROR}",
            "diagnostics": []
        }
    
    lang_key = language_str.upper().strip()
    if lang_key in ["C++", "CXX"]:
        lang_key = "CPP"
    elif lang_key in ["PY"]:
        lang_key = "PYTHON"
        
    if lang_key not in PARSERS:
        return {
            "status": "ERROR",
            "message": f"Unsupported language: {language_str}. Supported languages: C, CPP, PYTHON.",
            "diagnostics": []
        }
    
    try:
        source_bytes = source_code.encode('utf-8')
        parser = PARSERS[lang_key]
        tree = parser.parse(source_bytes)
        root = tree.root_node
        
        source_lines = source_code.splitlines()
        diagnostics = collect_diagnostics(root, source_lines)
        
        has_syntax_errors = root.has_error or len(diagnostics) > 0
        node_counter = [0]
        ast_dict = serialize_ast(root, source_bytes, node_counter)
        
        return {
            "status": "SYNTAX_ERROR" if has_syntax_errors else "SUCCESS",
            "language": lang_key,
            "hasErrors": has_syntax_errors,
            "diagnostics": diagnostics,
            "nodeCount": node_counter[0],
            "root": ast_dict
        }
    except Exception as e:
        return {
            "status": "ERROR",
            "message": f"Parsing exception: {str(e)}",
            "diagnostics": [{
                "severity": "FATAL",
                "line": 1,
                "column": 0,
                "endLine": 1,
                "endColumn": 0,
                "message": traceback.format_exc()
            }]
        }

def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--health":
        print(json.dumps(get_health(), indent=2))
        sys.exit(0)
        
    # Read entire input from stdin as JSON
    try:
        input_data = sys.stdin.read()
        if not input_data.strip():
            print(json.dumps({"status": "ERROR", "message": "Empty input payload"}))
            sys.exit(1)
            
        req = json.loads(input_data)
        action = req.get("action", "parse")
        if action == "health":
            print(json.dumps(get_health()))
            sys.exit(0)
            
        language = req.get("language", "")
        source_code = req.get("sourceCode", "")
        
        res = parse_code(language, source_code)
        print(json.dumps(res))
    except Exception as e:
        print(json.dumps({
            "status": "ERROR",
            "message": f"Internal parser CLI error: {str(e)}\n{traceback.format_exc()}"
        }))
        sys.exit(1)

if __name__ == "__main__":
    main()
