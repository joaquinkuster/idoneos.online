const fs = require('fs');
const path = require('path');
const docx = require('docx');
const { Document, Packer, Paragraph, TextRun, HeadingLevel, AlignmentType, BorderStyle, Table, TableRow, TableCell, WidthType, ShadingType } = docx;

// Tipografía y Paleta Corporativa idéntica a la imagen:
// Azul Títulos: #1B4D89 (Azul institucional)
// Acento Orador / Negritas: #0F172A (Negro azulado profundo)
// Texto Regular: #1E293B (Gris oscuro nítido)
// Líneas divisorias: #94A3B8

function parseFormattedRuns(text, defaultColor = "1E293B", defaultSize = 22) {
  const runs = [];
  // Regex para capturar **negrita**, *cursiva*, `código`
  const parts = text.split(/(\*\*.*?\*\*|\*.*?\*|`.*?`)/g);

  for (let part of parts) {
    if (!part) continue;
    if (part.startsWith('**') && part.endsWith('**')) {
      runs.push(new TextRun({
        text: part.slice(2, -2),
        bold: true,
        color: "0F172A",
        font: "Segoe UI",
        size: defaultSize
      }));
    } else if (part.startsWith('*') && part.endsWith('*')) {
      runs.push(new TextRun({
        text: part.slice(1, -1),
        italics: true,
        color: "334155",
        font: "Segoe UI",
        size: defaultSize
      }));
    } else if (part.startsWith('`') && part.endsWith('`')) {
      runs.push(new TextRun({
        text: part.slice(1, -1),
        font: "Consolas",
        color: "B45309",
        size: defaultSize - 1
      }));
    } else {
      runs.push(new TextRun({
        text: part,
        color: defaultColor,
        font: "Segoe UI",
        size: defaultSize
      }));
    }
  }

  return runs;
}

function parseMarkdownToDocx(mdContent) {
  const lines = mdContent.split(/\r?\n/);
  const children = [];
  let inCodeBlock = false;
  let codeBuffer = [];

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i];

    if (line.startsWith('```')) {
      if (inCodeBlock) {
        // Bloque de Código para el Prompt Gamma
        children.push(new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          rows: [
            new TableRow({
              children: [
                new TableCell({
                  shading: { type: ShadingType.CLEAR, fill: "F8FAFC" },
                  margins: { top: 160, bottom: 160, left: 200, right: 200 },
                  borders: {
                    top: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
                    bottom: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
                    left: { style: BorderStyle.SINGLE, size: 14, color: "1B4D89" },
                    right: { style: BorderStyle.SINGLE, size: 4, color: "CBD5E1" },
                  },
                  children: codeBuffer.map(cLine => new Paragraph({
                    children: [new TextRun({ text: cLine || " ", font: "Consolas", size: 18, color: "0F172A" })],
                    spacing: { before: 20, after: 20 }
                  }))
                })
              ]
            })
          ]
        }));
        children.push(new Paragraph({ spacing: { after: 120 } }));
        codeBuffer = [];
        inCodeBlock = false;
      } else {
        inCodeBlock = true;
        codeBuffer = [];
      }
      continue;
    }

    if (inCodeBlock) {
      codeBuffer.push(line);
      continue;
    }

    const trimmed = line.trim();
    if (!trimmed) {
      children.push(new Paragraph({ spacing: { after: 60 } }));
      continue;
    }

    // Separador horizontal <hr>
    if (trimmed === '---') {
      children.push(new Paragraph({
        border: {
          bottom: { style: BorderStyle.SINGLE, size: 8, color: "94A3B8" }
        },
        spacing: { before: 120, after: 160 }
      }));
      continue;
    }

    // Título Principal (# )
    if (line.startsWith('# ')) {
      children.push(new Paragraph({
        children: [
          new TextRun({
            text: line.replace('# ', ''),
            bold: true,
            color: "1B4D89", // Azul institucional idéntico a la imagen
            size: 32, // 16pt
            font: "Segoe UI"
          })
        ],
        spacing: { before: 240, after: 100 }
      }));
      continue;
    }

    // Título Secundario (## )
    if (line.startsWith('## ')) {
      children.push(new Paragraph({
        children: [
          new TextRun({
            text: line.replace('## ', ''),
            bold: true,
            color: "1B4D89",
            size: 26, // 13pt
            font: "Segoe UI"
          })
        ],
        spacing: { before: 200, after: 80 }
      }));
      continue;
    }

    // Título de Diapositiva (### )
    if (line.startsWith('### ')) {
      children.push(new Paragraph({
        children: [
          new TextRun({
            text: line.replace('### ', ''),
            bold: true,
            color: "1B4D89", // Como "2.2. Técnica de recolección de datos" en la captura
            size: 23, // 11.5pt
            font: "Segoe UI"
          })
        ],
        spacing: { before: 160, after: 60 }
      }));
      continue;
    }

    // Orador en negrita (ej: **Orador (Lázaro Martinez):**)
    if (trimmed.startsWith('**Orador') || trimmed.startsWith('Orador (')) {
      const cleanOrador = trimmed.replace(/\*\*/g, '');
      children.push(new Paragraph({
        children: [
          new TextRun({
            text: cleanOrador,
            bold: true,
            color: "0F172A",
            size: 22,
            font: "Segoe UI"
          })
        ],
        spacing: { before: 80, after: 40 }
      }));
      continue;
    }

    // Viñetas (*, •, -)
    if (trimmed.startsWith('• ') || trimmed.startsWith('* ') || trimmed.startsWith('- ')) {
      const bulletText = trimmed.replace(/^[•\*\-]\s+/, '');
      children.push(new Paragraph({
        children: parseFormattedRuns(bulletText),
        bullet: { level: 0 },
        spacing: { before: 20, after: 40 }
      }));
      continue;
    }

    // Listas numeradas (ej: 1. , 2. )
    if (/^\d+\.\s+/.test(trimmed)) {
      children.push(new Paragraph({
        children: parseFormattedRuns(trimmed),
        indent: { left: 360 },
        spacing: { before: 30, after: 50 }
      }));
      continue;
    }

    // Párrafo normal justificado
    children.push(new Paragraph({
      children: parseFormattedRuns(line),
      alignment: AlignmentType.JUSTIFIED,
      spacing: { before: 30, after: 70 }
    }));
  }

  return children;
}

async function convertFile(mdPath, docxPath, title) {
  const mdContent = fs.readFileSync(mdPath, 'utf8');
  const doc = new Document({
    title: title,
    styles: {
      default: {
        document: {
          run: {
            font: "Segoe UI",
            size: 22, // 11 pt
            color: "1E293B"
          },
          paragraph: {
            spacing: { line: 260 } // 1.15x
          }
        }
      }
    },
    sections: [{
      properties: {
        page: {
          margin: {
            top: 1440, // 1 in (2.54 cm)
            bottom: 1440,
            left: 1440,
            right: 1440
          }
        }
      },
      children: parseMarkdownToDocx(mdContent)
    }]
  });

  const buffer = await Packer.toBuffer(doc);
  fs.writeFileSync(docxPath, buffer);
  console.log(`Successfully generated updated DOCX: ${docxPath}`);
}

async function main() {
  const dir = __dirname;
  const targetMd = path.join(dir, 'guion_presentacion_dividido_y_prompt_gamma.md');
  const targetDocx = path.join(dir, 'guion_presentacion_dividido_y_prompt_gamma.docx');
  await convertFile(targetMd, targetDocx, "Guión de Exposición y Prompt Gamma - Idóneos Online");
}

main().catch(console.error);
