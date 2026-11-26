import React, { useState } from 'react';
import { ChevronRight, ChevronDown, Eye, Search, Layers, FileCode2, Maximize2, Minimize2 } from 'lucide-react';

function AstTreeNode({ node, selectedNode, onSelectNode, depth = 0, defaultExpanded = true }) {
  const [expanded, setExpanded] = useState(depth < 2 || defaultExpanded);
  const hasChildren = node.children && node.children.length > 0;
  const isSelected = selectedNode && selectedNode.id === node.id;

  // Status badge styling
  const getStatusBadge = (status) => {
    switch (status) {
      case 'MATCHED':
        return <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">MATCHED</span>;
      case 'SUBSTITUTED':
        return <span className="text-[10px] px-1.5 py-0.5 rounded bg-amber-950 text-amber-400 border border-amber-800">SUBSTITUTE</span>;
      case 'INSERTED':
        return <span className="text-[10px] px-1.5 py-0.5 rounded bg-sky-950 text-sky-400 border border-sky-800">INSERTED</span>;
      case 'DELETED':
        return <span className="text-[10px] px-1.5 py-0.5 rounded bg-rose-950 text-rose-400 border border-rose-800">DELETED</span>;
      default:
        return null;
    }
  };

  const getCategoryColor = (category) => {
    switch (category) {
      case 'PROGRAM': return 'text-purple-400';
      case 'FUNCTION_DECLARATION': return 'text-indigo-400';
      case 'VARIABLE_DECLARATION': return 'text-sky-400';
      case 'CONTROL_FLOW': return 'text-amber-400';
      case 'STATEMENT': return 'text-blue-400';
      case 'EXPRESSION': return 'text-teal-400';
      case 'IDENTIFIER': return 'text-emerald-400';
      case 'LITERAL': return 'text-orange-400';
      case 'OPERATOR': return 'text-rose-400';
      default: return 'text-slate-300';
    }
  };

  return (
    <div className="select-none font-mono text-xs">
      <div
        onClick={() => onSelectNode(node)}
        className={`flex items-center space-x-1.5 py-1 px-2 rounded-lg cursor-pointer transition-colors ${
          isSelected
            ? 'bg-sky-500/20 text-white border border-sky-500/40'
            : 'hover:bg-slate-800/60 text-slate-300'
        }`}
        style={{ paddingLeft: `${depth * 18 + 8}px` }}
      >
        {hasChildren ? (
          <button
            onClick={(e) => {
              e.stopPropagation();
              setExpanded(!expanded);
            }}
            className="p-0.5 hover:text-white text-slate-500"
          >
            {expanded ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
          </button>
        ) : (
          <span className="w-3.5 h-3.5 inline-block text-slate-600">•</span>
        )}

        <span className={`font-semibold ${getCategoryColor(node.category)}`}>
          {node.normalizedType || node.originalType}
        </span>

        {node.normalizedValue && (
          <span className="text-slate-400 bg-slate-800/80 px-1.5 py-0.2 rounded border border-slate-700/50">
            "{node.normalizedValue}"
          </span>
        )}

        {getStatusBadge(node.matchStatus)}

        {node.startLine > 0 && (
          <span className="text-[10px] text-slate-500 ml-auto">
            L{node.startLine}:{node.startCol}
          </span>
        )}
      </div>

      {hasChildren && expanded && (
        <div className="border-l border-slate-800/80 ml-3">
          {node.children.map((child) => (
            <AstTreeNode
              key={child.id}
              node={child}
              selectedNode={selectedNode}
              onSelectNode={onSelectNode}
              depth={depth + 1}
              defaultExpanded={defaultExpanded}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function AstVisualizer({ treeA, treeB }) {
  const [activeTree, setActiveTree] = useState('A');
  const [selectedNode, setSelectedNode] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [expandAll, setExpandAll] = useState(true);

  const currentTree = activeTree === 'A' ? treeA : treeB;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-2xl">
      {/* Header bar */}
      <div className="p-4 border-b border-slate-800 flex flex-wrap items-center justify-between gap-4 bg-slate-900/60">
        <div className="flex items-center space-x-3">
          <Layers className="w-5 h-5 text-sky-400" />
          <h3 className="font-semibold text-white">Genuine Abstract Syntax Tree Inspector</h3>
          <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
            Tree-sitter Derived
          </span>
        </div>

        <div className="flex items-center space-x-2">
          {/* Switch Tree A / Tree B */}
          <div className="bg-slate-800 p-0.5 rounded-lg border border-slate-700 flex">
            <button
              onClick={() => { setActiveTree('A'); setSelectedNode(null); }}
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                activeTree === 'A'
                  ? 'bg-sky-500 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Program A Tree
            </button>
            <button
              onClick={() => { setActiveTree('B'); setSelectedNode(null); }}
              className={`px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                activeTree === 'B'
                  ? 'bg-sky-500 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Program B Tree
            </button>
          </div>

          <button
            onClick={() => setExpandAll(!expandAll)}
            className="p-1.5 text-slate-400 hover:text-white bg-slate-800 border border-slate-700 rounded-lg text-xs flex items-center space-x-1"
            title={expandAll ? 'Collapse Subtrees' : 'Expand Subtrees'}
          >
            {expandAll ? <Minimize2 className="w-4 h-4" /> : <Maximize2 className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Main Grid: Tree View + Node Inspector Sidebar */}
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-[500px]">
        {/* Left: Tree View Hierarchy */}
        <div className="lg:col-span-8 p-4 border-r border-slate-800 overflow-y-auto max-h-[600px]">
          {currentTree ? (
            <AstTreeNode
              node={currentTree}
              selectedNode={selectedNode}
              onSelectNode={setSelectedNode}
              depth={0}
              defaultExpanded={expandAll}
            />
          ) : (
            <div className="text-center py-16 text-slate-500">
              <Layers className="w-10 h-10 mx-auto text-slate-600 mb-2" />
              <p>No AST generated. Parse or compare source code in the workspace.</p>
            </div>
          )}
        </div>

        {/* Right: Node Details Inspector */}
        <div className="lg:col-span-4 p-5 bg-slate-950/40">
          <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 flex items-center">
            <Eye className="w-4 h-4 mr-1.5 text-sky-400" /> Node Attributes
          </h4>

          {selectedNode ? (
            <div className="space-y-4 text-xs font-mono">
              <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                <span className="text-slate-500 block text-[10px] uppercase">Node Identifier</span>
                <span className="text-sky-300 font-bold">{selectedNode.id}</span>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                <span className="text-slate-500 block text-[10px] uppercase">Normalized Type</span>
                <span className="text-white font-bold">{selectedNode.normalizedType}</span>
                <span className="text-slate-400 block text-[11px] mt-0.5">Original: {selectedNode.originalType}</span>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                <span className="text-slate-500 block text-[10px] uppercase">Semantic Category</span>
                <span className="text-indigo-400 font-semibold">{selectedNode.category}</span>
              </div>

              {selectedNode.normalizedValue && (
                <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                  <span className="text-slate-500 block text-[10px] uppercase">Normalized Value</span>
                  <span className="text-emerald-400 font-bold">{selectedNode.normalizedValue}</span>
                  {selectedNode.rawValue && selectedNode.rawValue !== selectedNode.normalizedValue && (
                    <span className="text-slate-500 block text-[11px] mt-0.5">Raw: "{selectedNode.rawValue}"</span>
                  )}
                </div>
              )}

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                <span className="text-slate-500 block text-[10px] uppercase">Source Coordinates</span>
                <span className="text-slate-300">
                  Line {selectedNode.startLine}:{selectedNode.startCol} → Line {selectedNode.endLine}:{selectedNode.endCol}
                </span>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
                <span className="text-slate-500 block text-[10px] uppercase">Comparison Status</span>
                <div className="mt-1 flex items-center justify-between">
                  <span className="font-bold text-slate-200">{selectedNode.matchStatus}</span>
                  <span className="text-slate-400">Edit Cost: {selectedNode.editCost}</span>
                </div>
                {selectedNode.matchedNodeId && (
                  <span className="text-sky-400 text-[11px] block mt-1">
                    Matched with: {selectedNode.matchedNodeId}
                  </span>
                )}
              </div>
            </div>
          ) : (
            <div className="text-center py-12 text-slate-500">
              <FileCode2 className="w-8 h-8 mx-auto text-slate-600 mb-2" />
              <p>Select any node in the AST hierarchy to inspect its compiler metadata and alignment status.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
