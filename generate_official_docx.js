const fs = require('fs');
const path = require('path');
const docx = require('docx');

const {
  Document,
  Packer,
  Paragraph,
  TextRun,
  Table,
  TableRow,
  TableCell,
  WidthType,
  BorderStyle,
  AlignmentType
} = docx;

// 1. Leer Casos de Uso Reales.md
const mdPath = path.join(__dirname, 'docs', 'diseño', 'Casos de Uso Reales.md');
const md = fs.readFileSync(mdPath, 'utf8');

// Configuración de Estilos
const FONT_FAMILY = 'Calibri';
const COLOR_BLUE_HEADING = '2E75B6';
const COLOR_TEXT_DARK = '000000';
const COLOR_MUTED_BORDER = '7F7F7F';
const COLOR_TABLE_BORDER = '000000';
const COLOR_TABLE_HEADER_BG = 'EDEDED';

// Medidas exactas en DXA
// Tabla Principal: 9600 dxa (columna 1: 2600 dxa, columna 2: 7000 dxa)
const MASTER_COL_WIDTHS = [2600, 7000];
const MASTER_TOTAL_WIDTH = 9600;

// Tabla Anidada (Flujo de eventos en 2 columnas: Paso: 1000 dxa, Acción: 5300 dxa)
const NESTED_COL_WIDTHS = [1000, 5300];
const NESTED_TOTAL_WIDTH = 6300;

const tableBorders = {
  top: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
  bottom: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
  left: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
  right: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
  insideHorizontal: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
  insideVertical: { style: BorderStyle.SINGLE, size: 6, color: COLOR_TABLE_BORDER },
};

const cellMargins = {
  top: 100,
  bottom: 100,
  left: 140,
  right: 140
};

// Separar por Casos de Uso
const cuBlocks = md.split(/(?=### CU-)/g);
const docChildren = [];

// Encabezado Principal de Sección
docChildren.push(
  new Paragraph({
    children: [
      new TextRun({
        text: '4.8. Casos de Uso Reales',
        font: FONT_FAMILY,
        size: 32, // 16pt
        bold: true,
        color: COLOR_BLUE_HEADING
      })
    ],
    spacing: { after: 200 }
  })
);

docChildren.push(
  new Paragraph({
    children: [
      new TextRun({
        text: 'En esta sección se detallan los 100 casos de uso reales del Sistema Idóneos Online, derivados de los casos de uso extendidos y complementados con la información funcional y de interacción disponible en las pantallas y prototipos de interfaz de usuario. Cada caso de uso se referencia mediante un código correlativo (CU-01 a CU-99, incluyendo CU-26b).',
        font: FONT_FAMILY,
        size: 22, // 11pt
        color: COLOR_TEXT_DARK
      })
    ],
    spacing: { after: 240 }
  })
);

// Línea horizontal institucional
docChildren.push(
  new Paragraph({
    text: '',
    border: {
      bottom: { style: BorderStyle.SINGLE, size: 12, color: COLOR_MUTED_BORDER }
    },
    spacing: { after: 300 }
  })
);

// Helper para formatear texto con etiquetas [A], [B] en negrita
function formatTextWithBadges(text, fontSize = 20) {
  if (!text) return [new TextRun({ text: '', font: FONT_FAMILY, size: fontSize })];

  const runs = [];
  const regex = /\[([A-Z0-9]+)\]/g;
  let lastIdx = 0;
  let match;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIdx) {
      runs.push(new TextRun({
        text: text.substring(lastIdx, match.index),
        font: FONT_FAMILY,
        size: fontSize,
        color: COLOR_TEXT_DARK
      }));
    }
    runs.push(new TextRun({
      text: '[' + match[1] + ']',
      font: FONT_FAMILY,
      size: fontSize,
      bold: true,
      color: COLOR_TEXT_DARK
    }));
    lastIdx = match.index + match[0].length;
  }

  if (lastIdx < text.length) {
    runs.push(new TextRun({
      text: text.substring(lastIdx),
      font: FONT_FAMILY,
      size: fontSize,
      color: COLOR_TEXT_DARK
    }));
  }

  return runs.length > 0 ? runs : [new TextRun({ text, font: FONT_FAMILY, size: fontSize, color: COLOR_TEXT_DARK })];
}

// Helper para parsear tablas Markdown
function parseMarkdownTable(tableMd) {
  if (!tableMd) return null;
  const lines = tableMd.trim().split('\n').filter(l => l.includes('|'));
  if (lines.length < 2) return null;
  
  const headers = lines[0].split('|').slice(1, -1).map(c => c.trim());
  const rowsData = [];
  
  for (let i = 2; i < lines.length; i++) {
    const cells = lines[i].split('|').slice(1, -1).map(c => c.trim());
    if (cells.length > 0) {
      rowsData.push(cells);
    }
  }
  
  return { headers, rowsData };
}

// Helper para crear tabla interna anidada (Flujo de eventos en 2 columnas)
function createNestedTable(tableData) {
  if (!tableData) return new Paragraph({ text: '—', spacing: { before: 60, after: 60 } });

  const rows = [];
  
  // Fila Header de la tabla interna
  const headerCells = tableData.headers.map((h, idx) => {
    const width = NESTED_COL_WIDTHS[idx] || 5300;

    return new TableCell({
      width: { size: width, type: WidthType.DXA },
      shading: { fill: COLOR_TABLE_HEADER_BG },
      margins: { top: 60, bottom: 60, left: 100, right: 100 },
      children: [
        new Paragraph({
          children: [
            new TextRun({
              text: h,
              font: FONT_FAMILY,
              size: 19, // 9.5pt
              bold: true,
              color: COLOR_TEXT_DARK
            })
          ],
          alignment: idx === 0 ? AlignmentType.CENTER : AlignmentType.LEFT
        })
      ]
    });
  });
  rows.push(new TableRow({ children: headerCells }));

  // Filas de Datos
  tableData.rowsData.forEach(r => {
    const cells = r.map((c, idx) => {
      const width = NESTED_COL_WIDTHS[idx] || 5300;

      return new TableCell({
        width: { size: width, type: WidthType.DXA },
        margins: { top: 60, bottom: 60, left: 100, right: 100 },
        children: [
          new Paragraph({
            children: formatTextWithBadges(c, 19),
            alignment: idx === 0 ? AlignmentType.CENTER : AlignmentType.LEFT
          })
        ]
      });
    });
    rows.push(new TableRow({ children: cells }));
  });

  return new Table({
    rows,
    width: { size: NESTED_TOTAL_WIDTH, type: WidthType.DXA },
    columnWidths: NESTED_COL_WIDTHS,
    borders: tableBorders
  });
}

// Parsear y construir cada CU en su Tabla Maestra
let currentModule = '';

for (let i = 1; i < cuBlocks.length; i++) {
  const block = cuBlocks[i];
  
  // Extraer Título y Nombre
  const titleMatch = block.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) continue;
  
  const cuCode = titleMatch[1].trim();
  const cuName = titleMatch[2].trim();
  
  // Extraer Módulo
  const modMatch = block.match(/- \*\*Módulo\*\*:\s*([^\r\n]+)/);
  const cuModule = modMatch ? modMatch[1].trim() : 'Módulo General';
  
  // Si cambia el módulo, agregar el título del módulo en negrita
  if (cuModule !== currentModule) {
    currentModule = cuModule;
    docChildren.push(
      new Paragraph({
        children: [
          new TextRun({
            text: currentModule,
            font: FONT_FAMILY,
            size: 24, // 12pt
            bold: true,
            color: COLOR_TEXT_DARK
          })
        ],
        spacing: { before: 360, after: 180 }
      })
    );
  }

  // Extraer Campos
  const objMatch = block.match(/- \*\*Objetivo\(s\) asociado\(s\)\*\*:\s*([^\r\n]+)/);
  const objText = objMatch ? objMatch[1].trim() : '—';

  const reqMatch = block.match(/- \*\*Requisito\(s\) de información asociado\(s\)\*\*:\s*([^\r\n]+)/);
  const reqText = reqMatch ? reqMatch[1].trim() : '—';

  const actorMatch = block.match(/- \*\*Actor\(es\)\*\*:\s*([^\r\n]+)/);
  const actorText = actorMatch ? actorMatch[1].trim() : 'Usuario';

  const descMatch = block.match(/- \*\*Descripción\*\*:\s*([\s\S]*?)(?=- \*\*Precondición)/);
  const descText = descMatch ? descMatch[1].trim() : '—';

  const preMatch = block.match(/- \*\*Precondición\(es\)\*\*:\s*([\s\S]*?)(?=- \*\*Flujo)/);
  const preText = preMatch ? preMatch[1].trim().replace(/^\s*-\s*/gm, '•  ') : '—';

  const postMatch = block.match(/- \*\*Postcondición\(es\)\*\*:\s*([\s\S]*?)(?=- \*\*Excepciones)/);
  const salidaMatch = block.match(/- \*\*Salida\*\*:\s*([\s\S]*?)(?=- \*\*Excepciones)/);
  const postText = postMatch ? postMatch[1].trim().replace(/^\s*-\s*/gm, '•  ') : (salidaMatch ? salidaMatch[1].trim() : '—');

  // Extraer Flujo
  const flujoMatch = block.match(/- \*\*Flujo de eventos\*\*:\s*([\s\S]*?)(?=- \*\*Postcondición|- \*\*Salida|- \*\*Excepciones)/);
  const flujoTableData = flujoMatch ? parseMarkdownTable(flujoMatch[1]) : null;

  // Extraer Excepciones
  const excMatch = block.match(/- \*\*Excepciones\*\*:\s*([\s\S]*?)(?=- \*\*Frecuencia|- \*\*Estabilidad|- \*\*Comentarios|---|$)/);
  const excText = excMatch ? excMatch[1].trim().replace(/^\s*-\s*/gm, '•  ') : '•  No aplica.';

  // Construir Filas de la Tabla Maestra
  const masterRows = [];

  // 1. Fila Header: CU-XX | Nombre del CU
  masterRows.push(
    new TableRow({
      children: [
        new TableCell({
          width: { size: MASTER_COL_WIDTHS[0], type: WidthType.DXA },
          shading: { fill: COLOR_TABLE_HEADER_BG },
          margins: cellMargins,
          children: [
            new Paragraph({
              children: [
                new TextRun({
                  text: cuCode,
                  font: FONT_FAMILY,
                  size: 21,
                  bold: true,
                  color: COLOR_TEXT_DARK
                })
              ]
            })
          ]
        }),
        new TableCell({
          width: { size: MASTER_COL_WIDTHS[1], type: WidthType.DXA },
          shading: { fill: COLOR_TABLE_HEADER_BG },
          margins: cellMargins,
          children: [
            new Paragraph({
              children: [
                new TextRun({
                  text: cuName,
                  font: FONT_FAMILY,
                  size: 21,
                  bold: true,
                  color: COLOR_TEXT_DARK
                })
              ]
            })
          ]
        })
      ]
    })
  );

  // Helper para filas estándar clave-valor
  function addRow(label, contentParagraphs) {
    masterRows.push(
      new TableRow({
        children: [
          new TableCell({
            width: { size: MASTER_COL_WIDTHS[0], type: WidthType.DXA },
            margins: cellMargins,
            children: [
              new Paragraph({
                children: [
                  new TextRun({
                    text: label,
                    font: FONT_FAMILY,
                    size: 20,
                    bold: true,
                    color: COLOR_TEXT_DARK
                  })
                ]
              })
            ]
          }),
          new TableCell({
            width: { size: MASTER_COL_WIDTHS[1], type: WidthType.DXA },
            margins: cellMargins,
            children: Array.isArray(contentParagraphs) ? contentParagraphs : [contentParagraphs]
          })
        ]
      })
    );
  }

  // 2. Objetivo(s) asociado(s)
  addRow('Objetivo(s) asociado(s)', new Paragraph({
    children: [new TextRun({ text: objText, font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })]
  }));

  // 3. Requisito(s) de información asociado(s)
  addRow('Requisito(s) de información asociado(s)', new Paragraph({
    children: [new TextRun({ text: reqText, font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })]
  }));

  // 4. Módulo
  addRow('Módulo', new Paragraph({
    children: [new TextRun({ text: '•  ' + cuModule, font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })]
  }));

  // 5. Actor(es)
  addRow('Actor(es)', new Paragraph({
    children: [new TextRun({ text: actorText, font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })]
  }));

  // 6. Descripción
  addRow('Descripción', new Paragraph({
    children: [new TextRun({ text: descText, font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })]
  }));

  // 7. Precondición(es)
  const preParagraphs = preText.split('\n').map(p => new Paragraph({
    children: [new TextRun({ text: p.trim(), font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })],
    spacing: { after: 40 }
  }));
  addRow('Precondición(es)', preParagraphs);

  // 8. Flujo de eventos (Tabla anidada de 2 columnas)
  addRow('Flujo de eventos', [createNestedTable(flujoTableData)]);

  // 9. Postcondición(es) / Salida
  const postParagraphs = postText.split('\n').map(p => new Paragraph({
    children: [new TextRun({ text: p.trim(), font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })],
    spacing: { after: 40 }
  }));
  addRow(postMatch ? 'Postcondición(es)' : 'Salida', postParagraphs);

  // 10. Excepciones
  const excParagraphs = excText.split('\n').map(p => new Paragraph({
    children: [new TextRun({ text: p.trim(), font: FONT_FAMILY, size: 20, color: COLOR_TEXT_DARK })],
    spacing: { after: 40 }
  }));
  addRow('Excepciones', excParagraphs);

  // Añadir la tabla al documento con columnWidths explícito
  docChildren.push(
    new Table({
      rows: masterRows,
      width: { size: MASTER_TOTAL_WIDTH, type: WidthType.DXA },
      columnWidths: MASTER_COL_WIDTHS,
      borders: tableBorders
    })
  );

  // Espaciado entre tablas de Casos de Uso
  docChildren.push(
    new Paragraph({
      text: '',
      spacing: { after: 300 }
    })
  );
}

// 3. Crear el Documento Final
const doc = new Document({
  sections: [
    {
      properties: {
        page: {
          margin: {
            top: 1134, // 2 cm
            right: 1134, // 2 cm
            bottom: 1134, // 2 cm
            left: 1134  // 2 cm
          }
        }
      },
      children: docChildren
    }
  ]
});

// 4. Guardar archivo DOCX
const outputPath = path.join(__dirname, 'docs', 'diseño', 'Casos de Uso Reales.docx');
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(outputPath, buffer);
  console.log(`Successfully generated Casos de Uso Reales.docx (${cuBlocks.length - 1} CUs).`);
});
