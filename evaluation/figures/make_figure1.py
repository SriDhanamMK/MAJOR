#!/usr/bin/env python3
"""Figure 1 workflow: writes Figure1.drawio (editable, diagrams.net) and Figure1.png (preview rendered from the same spec)."""
import matplotlib; matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
from xml.sax.saxutils import escape

# (title, detail, fill, component)
STEPS = [
 ('1. Input Source Programs', 'Program P and Program Q (Monaco editors)', '#DAE8FC', 'React frontend'),
 ('2. Language Selection & Validation', 'C / C++ / Python; size and non-empty checks', '#DAE8FC', 'Spring Boot REST API'),
 ('3. AST Generation (Tree-sitter)', 'Parse tree + syntax-error diagnostics', '#FFF2CC', 'Python parser engine'),
 ('4. Structural Normalization', 'Common AST; comments/punctuation removed;\nscope-aware identifier labels; optional reordering', '#D5E8D4', 'Java (Spring Boot)'),
 ('5. Tree Edit Distance', 'Zhang-Shasha, graded substitution costs', '#D5E8D4', 'Java (Spring Boot)'),
 ('6. Similarity Score Calculation', 'S = 100 x (1 - D / (|T_P| + |T_Q|))', '#D5E8D4', 'Java (Spring Boot)'),
 ('7. Results & Visualization', 'Score (%), edit report, side-by-side view', '#DAE8FC', 'React frontend'),
]
W, H, GAP, X0, Y0 = 330, 66, 24, 40, 20

def drawio():
    cells = ['<mxCell id="0"/>', '<mxCell id="1" parent="0"/>']
    for i, (t, d, fill, comp) in enumerate(STEPS):
        y = Y0 + i * (H + GAP)
        val = f'&lt;b&gt;{escape(t)}&lt;/b&gt;&lt;br&gt;{escape(d).replace(chr(10), "&lt;br&gt;")}'
        cells.append(f'<mxCell id="s{i}" value="{val}" style="rounded=1;whiteSpace=wrap;html=1;fillColor={fill};strokeColor=#555555;fontSize=11;" vertex="1" parent="1"><mxGeometry x="{X0}" y="{y}" width="{W}" height="{H}" as="geometry"/></mxCell>')
        cells.append(f'<mxCell id="c{i}" value="{escape(comp)}" style="text;html=1;align=left;verticalAlign=middle;fontSize=10;fontStyle=2;fontColor=#555555;" vertex="1" parent="1"><mxGeometry x="{X0 + W + 14}" y="{y + 23}" width="170" height="20" as="geometry"/></mxCell>')
        if i:
            cells.append(f'<mxCell id="e{i}" style="endArrow=block;html=1;strokeColor=#333333;" edge="1" parent="1" source="s{i-1}" target="s{i}"><mxGeometry relative="1" as="geometry"/></mxCell>')
    h = Y0 + len(STEPS) * (H + GAP)
    return ('<mxfile host="app.diagrams.net"><diagram name="Figure 1" id="fig1"><mxGraphModel dx="800" dy="900" grid="1" gridSize="10" page="1" pageWidth="640" pageHeight="%d">'
            '<root>%s</root></mxGraphModel></diagram></mxfile>' % (h + 40, ''.join(cells)))

def png():
    n = len(STEPS); fig_h = Y0 * 2 + n * (H + GAP)
    fig, ax = plt.subplots(figsize=(6.4, fig_h / 100)); ax.set_xlim(0, 640); ax.set_ylim(fig_h, 0); ax.axis('off')
    for i, (t, d, fill, comp) in enumerate(STEPS):
        y = Y0 + i * (H + GAP)
        ax.add_patch(FancyBboxPatch((X0, y), W, H, boxstyle='round,pad=0,rounding_size=8', fc=fill, ec='#555555', lw=1))
        ax.text(X0 + W / 2, y + 17, t, ha='center', va='center', fontsize=8.5, fontweight='bold')
        ax.text(X0 + W / 2, y + 42, d, ha='center', va='center', fontsize=7)
        ax.text(X0 + W + 14, y + H / 2, comp, ha='left', va='center', fontsize=7.5, style='italic', color='#555555')
        if i: ax.add_patch(FancyArrowPatch((X0 + W / 2, y - GAP), (X0 + W / 2, y), arrowstyle='-|>', mutation_scale=10, color='#333333'))
    fig.savefig('Figure1.png', dpi=300, bbox_inches='tight'); fig.savefig('Figure1.pdf', bbox_inches='tight')

open('Figure1.drawio', 'w').write(drawio()); png()
