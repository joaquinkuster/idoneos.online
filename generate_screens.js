const fs = require('fs');
const path = require('path');

// 1. Read Casos de Uso Reales
const mdPath = path.join(__dirname, 'docs', 'diseño', 'Casos de Uso Reales.md');
const mdContent = fs.readFileSync(mdPath, 'utf8');

// 2. Read DSS.md for exact message traceability
const dssPath = path.join(__dirname, 'docs', 'analisis', 'DSS.md');
let dssContent = '';
try {
  dssContent = fs.readFileSync(dssPath, 'utf8');
} catch(e) {}

// Parse DSS messages by CU-XX
const dssMap = {};
if (dssContent) {
  const dssBlocks = dssContent.split(/(?=### CU-)/g);
  for (let b of dssBlocks) {
    const m = b.match(/### (CU-[^:\r\n]+):/);
    if (m) {
      const cuId = m[1].trim();
      const msgs = [];
      const msgRegex = /`([a-zA-Z0-9_]+\([^)]*\))`|\$?\\rightarrow\$?\s*`([^`]+)`/g;
      let mm;
      while ((mm = msgRegex.exec(b)) !== null) {
        const found = mm[1] || mm[2];
        if (found && !msgs.includes(found)) msgs.push(found);
      }
      dssMap[cuId] = msgs.slice(0, 3).join(' ➔ ');
    }
  }
}
dssMap['CU-26b'] = 'verContenidoUnidad(unaUnidad) ➔ ModoEdición';

// 3. Heroicons SVG Library (Full Outline Heroicons v2)
const icons = {
  academicCap: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M4.26 10.147a60.438 60.438 0 0 0-.491 6.347A48.62 48.62 0 0 1 12 20.904a48.62 48.62 0 0 1 8.232-4.41 60.46 60.46 0 0 0-.491-6.347m-15.482 0a50.636 50.636 0 0 0-2.658-.813A59.906 59.906 0 0 1 12 3.493a59.903 59.903 0 0 1 10.399 5.84c-.896.248-1.783.52-2.658.814m-15.482 0A50.717 50.717 0 0 1 12 13.489a50.702 50.702 0 0 1 7.74-3.342M6.75 15a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Zm0 0v-3.675A55.378 55.378 0 0 1 12 8.443m-5.25 6.557c1.764.39 3.58.648 5.25.75" /></svg>`,
  briefcase: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 0 0 .75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 0 0-3.413-.387m4.5 8.006c-.194.165-.42.295-.673.38A23.978 23.978 0 0 1 12 15.75c-2.648 0-5.195-.429-7.577-1.22a2.016 2.016 0 0 1-.673-.38m0 0A2.18 2.18 0 0 1 3 12.489V8.706c0-1.081.768-2.015 1.837-2.175a48.111 48.111 0 0 1 3.413-.387m7.5 0V5.25A2.25 2.25 0 0 0 13.5 3h-3a2.25 2.25 0 0 0-2.25 2.25v.894m7.5 0a48.667 48.667 0 0 0-7.5 0M12 12.75h.008v.008H12v-.008Z" /></svg>`,
  bolt: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m3.75 13.5 10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75Z" /></svg>`,
  documentText: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z" /></svg>`,
  videoCamera: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m15.75 10.5 4.72-4.72a.75.75 0 0 1 1.28.53v11.38a.75.75 0 0 1-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25h-9A2.25 2.25 0 0 0 2.25 7.5v9a2.25 2.25 0 0 0 2.25 2.25Z" /></svg>`,
  presentationChart: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3v11.25A2.25 2.25 0 0 0 6 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0 1 18 16.5h-2.25m-7.5 0h7.5m-7.5 0-1 3m8.5-3 1 3m0 0 .5 1.5m-.5-1.5h-9.5m0 0-.5 1.5m.75-9 3-3 2.143 2.143L15.75 6" /></svg>`,
  bookOpen: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 0 0 6 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 0 1 6 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 0 1 6-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0 0 18 18a8.967 8.967 0 0 0-6 2.292m0-14.25v14.25" /></svg>`,
  clipboardCheck: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M11.35 3.836c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m8.9-4.414c.376.023.75.05 1.124.08 1.131.09 1.976 1.053 1.976 2.188V19.5a2.25 2.25 0 0 1-2.25 2.25H6.75A2.25 2.25 0 0 1 4.5 19.5V6.25c0-1.135.845-2.098 1.976-2.188a48.57 48.57 0 0 1 1.124-.08C7.6 3.98 7.6 3.98 7.6 3.98m3.75 9.75 2.25 2.25 4.5-4.5" /></svg>`,
  queueList: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 12h16.5m-16.5 3.75h16.5M3.75 19.5h16.5M5.625 4.5h12.75a1.875 1.875 0 0 1 0 3.75H5.625a1.875 1.875 0 0 1 0-3.75Z" /></svg>`,
  chatBubble: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H8.25m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0H12m4.125 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a.75.75 0 0 1-.974-.94 5.95 5.95 0 0 0 .97-2.613A8.136 8.136 0 0 1 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z" /></svg>`,
  checkCircle: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" /></svg>`,
  lockClosed: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 0 0 2.25-2.25v-6.75a2.25 2.25 0 0 0-2.25-2.25H6.75a2.25 2.25 0 0 0-2.25 2.25v6.75a2.25 2.25 0 0 0 2.25 2.25Z" /></svg>`,
  cog6Tooth: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.594 3.94c.09-.542.56-.94 1.11-.94h2.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 0 1 1.37.49l1.296 2.247a1.125 1.125 0 0 1-.26 1.431l-1.003.827c-.293.24-.438.613-.431.992a6.759 6.759 0 0 1 0 .255c-.007.378.138.75.43.99l1.005.828c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 0 1-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.57 6.57 0 0 1-.22.128c-.331.183-.581.495-.644.869l-.213 1.28c-.09.543-.56.941-1.11.941h-2.594c-.55 0-1.02-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 0 1-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 0 1-1.369-.49l-1.297-2.247a1.125 1.125 0 0 1 .26-1.431l1.004-.827c.292-.24.437-.613.43-.992a6.932 6.932 0 0 1 0-.255c.007-.378-.138-.75-.43-.99l-1.004-.828a1.125 1.125 0 0 1-.26-1.43l1.297-2.247a1.125 1.125 0 0 1 1.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.087.22-.128.332-.183.582-.495.644-.869l.214-1.281Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /></svg>`,
  plus: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" /></svg>`,
  pencilSquare: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10" /></svg>`,
  trash: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" /></svg>`,
  bars3: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" /></svg>`,
  arrowRight: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" /></svg>`,
  exclamationTriangle: (cls="w-6 h-6") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z" /></svg>`,
  chevronDown: (cls="w-4 h-4") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5" /></svg>`,
  sparkles: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904 9 18.75l-.813-2.846a4.5 4.5 0 0 0-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 0 0 3.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 0 0 3.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 0 0-3.09 3.09ZM18.259 8.715 18 9.75l-.259-1.035a3.375 3.375 0 0 0-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 0 0 2.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 0 0 2.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 0 0-2.456 2.456ZM16.894 20.567 16.5 21.75l-.394-1.183a2.25 2.25 0 0 0-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 0 0 1.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 0 0 1.423 1.423l1.183.394-1.183.394a2.25 2.25 0 0 0-1.423 1.423Z" /></svg>`,
  microphone: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 18.75a6 6 0 0 0 6-6v-1.5m-6 7.5a6 6 0 0 1-6-6v-1.5m6 7.5v3.75m-3.75 0h7.5M12 15a3 3 0 0 0 3-3V4.5a3 3 0 0 0-6 0v7.5a3 3 0 0 0 3 3Z" /></svg>`,
  camera: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6.827 6.175A2.31 2.31 0 0 1 5.186 7.23c-.38.054-.757.112-1.134.175C2.999 7.58 2.25 8.507 2.25 9.574V18a2.25 2.25 0 0 0 2.25 2.25h15A2.25 2.25 0 0 0 21.75 18V9.574c0-1.067-.75-1.994-1.802-2.169a47.865 47.865 0 0 0-1.134-.175 2.31 2.31 0 0 1-1.64-1.055l-.822-1.316a2.192 2.192 0 0 0-1.736-1.039 48.774 48.774 0 0 0-5.232 0 2.192 2.192 0 0 0-1.736 1.039l-.821 1.316Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M16.5 12.75a4.5 4.5 0 1 1-9 0 4.5 4.5 0 0 1 9 0ZM18.75 10.5h.008v.008h-.008V10.5Z" /></svg>`,
  signal: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.348 14.652a3.75 3.75 0 0 1 0-5.304m5.304 0a3.75 3.75 0 0 1 0 5.304m-7.425 2.121a6.75 6.75 0 0 1 0-9.546m9.546 0a6.75 6.75 0 0 1 0 9.546M5.106 18.894c-3.808-3.807-3.808-9.98 0-13.788m13.788 0c3.808 3.807 3.808 9.98 0 13.788M12 12h.008v.008H12V12Z" /></svg>`,
  creditCard: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M2.25 8.25h19.5M2.25 9h19.5m-16.5 5.25h6m-6 2.25h3m-3.75 3h15a2.25 2.25 0 0 0 2.25-2.25V6.75A2.25 2.25 0 0 0 19.5 4.5h-15a2.25 2.25 0 0 0-2.25 2.25v10.5A2.25 2.25 0 0 0 4.5 19.5Z" /></svg>`,
  chartBar: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" /></svg>`,
  user: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z" /></svg>`,
  users: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 0 0 2.625.372 9.337 9.337 0 0 0 4.121-.952 4.125 4.125 0 0 0-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 0 1 8.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0 1 11.964-3.07M12 6.375a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0Zm8.25 2.25a2.625 2.625 0 1 1-5.25 0 2.625 2.625 0 0 1 5.25 0Z" /></svg>`,
  shieldCheck: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75m-3-7.036A11.959 11.959 0 0 1 3.598 6 11.99 11.99 0 0 0 3 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285Z" /></svg>`,
  tag: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 0 0 3 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 0 0 5.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 0 0 9.568 3Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6Z" /></svg>`,
  bell: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0" /></svg>`,
  magnifyingGlass: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z" /></svg>`,
  xMark: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" /></svg>`,
  photo: (cls="w-5 h-5") => `<svg class="${cls}" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z" /></svg>`
};

// 4. Parse Markdown CUs
const cuBlocks = mdContent.split(/(?=### CU-)/g);
const cus = [];

for (let i = 1; i < cuBlocks.length; i++) {
  const block = cuBlocks[i];
  const titleMatch = block.match(/### (CU-[^:\r\n]+):\s*([^\r\n]+)/);
  if (!titleMatch) continue;
  
  const id = titleMatch[1].trim();
  const name = titleMatch[2].trim();
  
  // Soporte tanto para formato en línea (**Módulo**: ...) como con salto de línea
  const moduleMatch = block.match(/\*\*Módulo\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Módulo\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const actorsMatch = block.match(/\*\*Actor\(es\)\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Actor\(es\)\*\*\s*[\r\n]+-\s*([^\r\n]+)/);
  const descMatch = block.match(/\*\*Descripción\*\*:\s*([^\r\n]+)/) || block.match(/\*\*Descripción\*\*\s*[\r\n]+([\s\S]*?)(?=\*\*Precondición|\*\*Flujo)/);
  
  const badges = [];
  const badgeRegex = /\[([A-Z0-9]+)\]/g;
  let bm;
  while ((bm = badgeRegex.exec(block)) !== null) {
    if (!badges.includes(bm[1])) badges.push(bm[1]);
  }
  
  cus.push({
    id,
    name,
    module: moduleMatch ? moduleMatch[1].trim() : 'Módulo General',
    actors: actorsMatch ? actorsMatch[1].trim() : 'Usuario',
    description: descMatch ? descMatch[1].trim() : '',
    badges: badges.length > 0 ? badges : ['A', 'B', 'C', 'D']
  });
}

// Group by Module for left sidebar
const modules = [];
const moduleMap = {};
cus.forEach(c => {
  if (!moduleMap[c.module]) {
    moduleMap[c.module] = [];
    modules.push({ name: c.module, cus: moduleMap[c.module] });
  }
  moduleMap[c.module].push(c);
});

// Role & Dropdown Helper (Clean & organized)
function getRoleInfo(actors, cuId) {
  const a = (actors || '').toLowerCase();
  const isEditingMode = ['CU-26b', 'CU-19', 'CU-20', 'CU-21', 'CU-22', 'CU-27', 'CU-28', 'CU-29', 'CU-30', 'CU-31', 'CU-32', 'CU-33', 'CU-34', 'CU-53', 'CU-54', 'CU-55', 'CU-56', 'CU-57', 'CU-58', 'CU-59', 'CU-60', 'CU-65', 'CU-66', 'CU-67', 'CU-68', 'CU-69', 'CU-70', 'CU-71'].includes(cuId);

  if (a.includes('alumno') && !a.includes('docente') && !a.includes('administrador')) {
    return {
      role: 'Alumno',
      name: 'Joaquín Küster',
      email: 'joaquin.kuster@idoneos.online',
      initials: 'JK',
      isDocente: false,
      isAdmin: false,
      dropdownSections: [
        {
          title: 'Navegación',
          items: [
            { label: 'Mis cursos matriculados', cu: 'CU-02' },
            { label: 'Explorar catálogo abierto', cu: 'CU-06' },
            { label: 'Historial de pagos e inscripciones', cu: 'CU-46' },
            { label: 'Mis calificaciones obtenidas', cu: 'CU-62' }
          ]
        },
        {
          title: 'Cuenta',
          items: [
            { label: 'Ver mi perfil', cu: 'CU-86' },
            { label: 'Editar datos de perfil', cu: 'CU-87' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  } else if (a.includes('docente') && !a.includes('administrador')) {
    return {
      role: 'Docente Titular',
      name: 'Lic. Fausto Spotorno',
      email: 'fausto.spotorno@idoneos.online',
      initials: 'FS',
      isDocente: true,
      isAdmin: false,
      dropdownSections: [
        {
          title: 'Modo Edición',
          hasEditingToggle: true,
          isEditingActive: isEditingMode
        },
        {
          title: 'Gestión de Cursos',
          items: [
            { label: 'Mis cursos a cargo', cu: 'CU-01' },
            { label: 'Estructura de unidades', cu: 'CU-19' },
            { label: 'Cronograma y avance', cu: 'CU-23' },
            { label: 'Lista de participantes', cu: 'CU-25' },
            { label: 'Estudio de Clon IA (HeyGen)', cu: 'CU-76' }
          ]
        },
        {
          title: 'Cuenta & Sesión',
          items: [
            { label: 'Mi perfil docente', cu: 'CU-86' },
            { label: 'Editar perfil', cu: 'CU-87' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  } else {
    return {
      role: 'Administrador',
      name: 'Admin General',
      email: 'admin@idoneos.online',
      initials: 'AG',
      isDocente: false,
      isAdmin: true,
      dropdownSections: [
        {
          title: 'Modo Edición',
          hasEditingToggle: true,
          isEditingActive: isEditingMode
        },
        {
          title: 'Administración',
          items: [
            { label: 'Gestión de cursos', cu: 'CU-01' },
            { label: 'Cohortes y programas', cu: 'CU-11' },
            { label: 'Gestión de usuarios', cu: 'CU-82' },
            { label: 'Auditoría de eventos', cu: 'CU-95' },
            { label: 'Reportes y estadísticas', cu: 'CU-98' },
            { label: 'Configuración general', cu: 'CU-99' }
          ]
        },
        {
          title: 'Cuenta',
          items: [
            { label: 'Perfil de administrador', cu: 'CU-86' },
            { label: 'Cerrar sesión', cu: 'CU-91', isDanger: true }
          ]
        }
      ]
    };
  }
}

// 5. Generate Wireframe Content
function generateScreenContent(cu) {
  const id = cu.id;
  const name = cu.name;
  const badges = cu.badges;

  // --- TYPE 1: CARDS VIEW (Mis Cursos / Catálogo) --- CU-01, CU-02, CU-06
  if (['CU-01', 'CU-02', 'CU-06'].includes(id)) {
    const isDocente = id === 'CU-01';
    const isAlumno = id === 'CU-02';
    const isCatalog = id === 'CU-06';

    return `
      <div class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Buscar cursos por título o palabra clave</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ej: Planificación Fiscal, Cripto, Mercado de Capitales...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtrar por Categoría / Estado</label>
            <div class="wf-select-container">
              <div class="wf-input-wrap">
                <div class="wf-input wf-select-trigger">
                  <span>Todas las categorías</span>
                  ${icons.chevronDown()}
                </div>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div class="wf-dropdown-menu">
                <div class="wf-dropdown-item active">☑ Todas las categorías</div>
                <div class="wf-dropdown-item">☐ Impuestos y Contabilidad</div>
                <div class="wf-dropdown-item">☐ Mercado de Capitales</div>
                <div class="wf-dropdown-item">☐ Finanzas y Cripto</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100">${isDocente ? 'Buscar Cursos' : (isAlumno ? 'Filtrar' : 'Explorar')}</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-cards-grid">
        <!-- Curso 1: Planificación Fiscal -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Impuestos y Contabilidad</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Planificación Fiscal y Tributaria</h4>
            <p class="wf-course-desc">Estrategias impositivas para pymes y profesionales.</p>
            <div class="wf-course-info-row">
              <span>📄 1 Programas</span>
              <span class="wf-course-price">$220.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            <div class="d-flex align-items-center justify-content-between gap-2">
              <button class="wf-btn-manage-course">
                ${icons.cog6Tooth("w-4 h-4")}
                <span>${isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle')}</span>
              </button>
              <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
            </div>
          </div>
        </div>

        <!-- Curso 2: Operativa Cripto -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Mercado de Capitales</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Operativa Cripto y DeFi</h4>
            <p class="wf-course-desc">Trading, DeFi y custodia institucional.</p>
            <div class="wf-course-info-row">
              <span>📄 1 Programas</span>
              <span class="wf-course-price">$120.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            <button class="wf-btn-manage-course">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>${isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle')}</span>
            </button>
          </div>
        </div>

        <!-- Curso 3: Mercado de Capitales Argentino -->
        <div class="wf-course-card">
          <div class="wf-course-card-thumb">
            <div class="wf-course-thumb-icon">${icons.academicCap("w-8 h-8 text-white")}</div>
            <div class="wf-course-pills-row">
              <span class="wf-pill-tag">Mercado de Capitales</span>
              <span class="wf-pill-status">Sin cohortes abiertas</span>
            </div>
          </div>
          <div class="wf-course-card-body">
            <h4 class="wf-course-title">Mercado de Capitales Argentino</h4>
            <p class="wf-course-desc">Análisis de bonos, acciones y estructura de la CNV.</p>
            <div class="wf-course-info-row">
              <span>📄 2 Programas</span>
              <span class="wf-course-price">$45.000</span>
            </div>
          </div>
          <div class="wf-course-card-footer">
            <button class="wf-btn-manage-course">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>${isDocente ? 'Gestionar Curso' : (isAlumno ? 'Ingresar al Curso' : 'Ver Detalle')}</span>
            </button>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 2: VISTA DEL CURSO ESTILO MOODLE (ALUMNO) --- CU-26, CU-62
  if (id === 'CU-26') {
    return `
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <div class="wf-breadcrumbs small text-muted mb-1">
              <span>Mis cursos</span> ➔ <span>Mercado de Capitales</span> ➔ <strong>Especialización en Idoneidad Bursátil</strong>
            </div>
            <h3 class="wf-course-main-title">Especialización en Idoneidad Bursátil (Cohorte 2026-1)</h3>
            <p class="small text-muted">Docente Titular: Lic. Fausto Spotorno | Duración: 8 Semanas | Programa Vigente</p>
          </div>
          <div class="text-end">
            <span class="wf-badge status-active">Inscripción Vigente</span>
            <div class="small text-muted mt-1">Progreso general: <strong>65%</strong></div>
          </div>
        </div>

        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-4">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[0] || 'A'}</span> Curso</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[4] || 'E'}</span> Participantes</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[5] || 'F'}</span> Calificaciones</button>
            <button class="wf-tab-btn"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Cronograma</button>
            <button class="wf-tab-btn">Insignias</button>
            <button class="wf-tab-btn">Competencias</button>
            <button class="wf-tab-btn">Más ▾</button>
          </div>
        </div>
      </div>

      <div class="wf-moodle-accordion">
        <div class="wf-unit-box completed mb-3">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <span class="text-success">${icons.checkCircle("w-5 h-5")}</span>
              <strong>Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</strong>
            </div>
            <span class="wf-badge status-active">Aprobada (Nota: 9/10)</span>
          </div>
          <div class="wf-unit-body">
            <ul class="wf-content-list">
              <li class="d-flex align-items-center gap-2">${icons.documentText("w-4 h-4 text-muted")} <a href="#" class="wf-link">Ley 26.831 y modificatorias (PDF - 2.4 MB)</a></li>
              <li class="d-flex align-items-center gap-2">${icons.videoCamera("w-4 h-4 text-muted")} <a href="#" class="wf-link">Grabación: Estructura del Mercado Argentino (45 min)</a></li>
              <li class="d-flex align-items-center gap-2">${icons.bookOpen("w-4 h-4 text-muted")} <a href="#" class="wf-link">Glosario de Términos CNV (12 términos)</a></li>
            </ul>
          </div>
        </div>

        <div class="wf-unit-box active mb-3">
          <div class="wf-unit-header d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <span class="text-navy">${icons.bookOpen("w-5 h-5")}</span>
              <strong>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</strong>
            </div>
            <div class="d-flex align-items-center gap-2">
              <span class="wf-badge status-active">En Curso</span>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
          <div class="wf-unit-body">
            <p class="small text-muted mb-3">Conceptos de TIR, Duration, Modified Duration y curvas de rendimiento soberanas.</p>
            <div class="wf-subcontent-group">
              <div class="wf-subcontent-title">Materiales de Estudio</div>
              <ul class="wf-content-list">
                <li class="d-flex align-items-center gap-2">${icons.documentText("w-4 h-4 text-muted")} <a href="#" class="wf-link">Guía Teórica de Renta Fija v2.1 (PDF)</a></li>
                <li class="d-flex align-items-center gap-2">${icons.presentationChart("w-4 h-4 text-muted")} <a href="#" class="wf-link">Planilla Excel: Cálculo de TIR y Flujos de Fondos</a></li>
              </ul>
            </div>

            <div class="wf-subcontent-group mt-3">
              <div class="wf-subcontent-title">Actividades & Evaluaciones</div>
              <ul class="wf-content-list">
                <li>
                  <div class="d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center gap-2">
                      ${icons.clipboardCheck("w-4 h-4 text-navy")}
                      <span><a href="#CU-63" class="wf-link"><strong>Autoevaluación Unidad 2: Ejercicios de Rendimiento</strong></a> (3 intentos máx.)</span>
                    </div>
                    <span class="pin-badge">${badges[3] || 'D'}</span>
                  </div>
                </li>
                <li class="d-flex align-items-center gap-2">${icons.chatBubble("w-4 h-4 text-muted")} <a href="#CU-35" class="wf-link">Foro de Consultas: Dudas sobre Duración Modificada</a> (4 consultas)</li>
                <li class="d-flex align-items-center gap-2">${icons.videoCamera("w-4 h-4 text-danger")} <a href="#CU-72" class="wf-link">Clase en Vivo: Streaming interactivo</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 3: MODO EDICIÓN DEL CURSO --- CU-26b, CU-19, CU-20, CU-21, CU-22
  if (['CU-26b', 'CU-19', 'CU-20', 'CU-21', 'CU-22'].includes(id)) {
    return `
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <h2 class="wf-course-main-title" style="font-size: 24px; font-weight: 700; color: #1E293B;">Especialización en Idoneidad Bursátil</h2>
            <div class="wf-breadcrumbs small text-muted mt-1" style="color: #D97706;">
              <span>Página Principal</span> / <span>Mis cursos</span> / <span>Idoneidad Bursátil</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <div class="d-flex align-items-center gap-2 bg-light p-2 border rounded">
              <span class="wf-badge status-active">Modo Edición ACTIVO</span>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
            <div class="text-muted cursor-pointer" title="Ajustes del curso">${icons.cog6Tooth("w-6 h-6 text-slate-600")}</div>
          </div>
        </div>

        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-1">
            <button class="wf-tab-btn active"><span class="pin-badge me-1">${badges[1] || 'B'}</span> Curso & Unidades</button>
            <a href="#CU-27" class="wf-tab-btn">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn">Pools</a>
            <a href="#CU-35" class="wf-tab-btn">Foros</a>
            <a href="#CU-65" class="wf-tab-btn">Clases en Vivo</a>
            <span class="wf-tab-btn text-muted">Más ▾</span>
          </div>
          <div class="d-flex align-items-center gap-2 pb-1">
            <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
              ${icons.plus()}
              <span>Añadir secciones</span>
            </button>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <div class="wf-moodle-accordion">
        <div class="wf-unit-box mb-3 p-3 bg-white border rounded">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="d-flex align-items-center gap-2">
              <span class="text-muted cursor-move">${icons.bars3("w-5 h-5")}</span>
              <h4 style="font-size: 16px; font-weight: 700; color: #D97706; margin: 0;">Unidad 1: Marco Regulatorio y Ley de Mercado de Capitales</h4>
              <span class="text-muted">${icons.pencilSquare("w-4 h-4")}</span>
            </div>
            <span class="small text-muted">Editar ▾</span>
          </div>

          <div class="wf-unit-body p-0">
            <div class="wf-content-item-row d-flex justify-content-between align-items-center py-2 border-bottom">
              <div class="d-flex align-items-center gap-2 ps-3">
                <span class="text-muted">${icons.bars3("w-4 h-4")}</span>
                ${icons.documentText("w-4 h-4 text-muted")}
                <span>Ley 26.831 y modificatorias (PDF - 2.4 MB)</span>
                <span class="text-muted">${icons.pencilSquare("w-3 h-3")}</span>
              </div>
              <span class="small text-muted pe-2">Editar ▾</span>
            </div>

            <div class="d-flex justify-content-end align-items-center gap-2 mt-3 pt-2">
              <a href="#CU-28" class="text-decoration-none small fw-bold d-flex align-items-center gap-1 p-2 rounded" style="color: #1E293B; background: #F1F5F9;">
                ${icons.plus("w-4 h-4")} <strong>Añade una actividad o un recurso</strong>
              </a>
              <span class="pin-badge">${badges[3] || badges[4] || 'D'}</span>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- TYPE 4: LISTADOS CONTEXTUALES DE RECURSOS --- CU-27, CU-31, CU-35, CU-53, CU-57, CU-65, CU-25
  if (['CU-27', 'CU-31', 'CU-35', 'CU-53', 'CU-57', 'CU-65', 'CU-25'].includes(id)) {
    const isMaterial = id === 'CU-27';
    const isGlosario = id === 'CU-31';
    const isForo = id === 'CU-35';
    const isPool = id === 'CU-53';
    const isEval = id === 'CU-57';
    const isLive = id === 'CU-65';

    return `
      <div class="wf-course-header-banner mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <h2 class="wf-course-main-title" style="font-size: 24px; font-weight: 700; color: #1E293B;">Especialización en Idoneidad Bursátil</h2>
            <div class="wf-breadcrumbs small text-muted mt-1" style="color: #D97706;">
              <span>Página Principal</span> / <span>Mis cursos</span> / <span>Idoneidad Bursátil</span> / <span>${name}</span>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge status-active">Curso Seleccionado</span>
            <div class="text-muted cursor-pointer" title="Ajustes del curso">${icons.cog6Tooth("w-6 h-6 text-slate-600")}</div>
          </div>
        </div>

        <div class="wf-course-nav-tabs mt-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-1">
            <a href="#CU-26b" class="wf-tab-btn">Curso & Unidades</a>
            <a href="#CU-27" class="wf-tab-btn ${isMaterial ? 'active' : ''}">Materiales</a>
            <a href="#CU-31" class="wf-tab-btn ${isGlosario ? 'active' : ''}">Glosario</a>
            <a href="#CU-57" class="wf-tab-btn ${isEval ? 'active' : ''}">Autoevaluaciones</a>
            <a href="#CU-53" class="wf-tab-btn ${isPool ? 'active' : ''}">Pools</a>
            <a href="#CU-35" class="wf-tab-btn ${isForo ? 'active' : ''}">Foros</a>
            <a href="#CU-65" class="wf-tab-btn ${isLive ? 'active' : ''}">Clases</a>
            <span class="wf-tab-btn text-muted">Más ▾</span>
          </div>
          <div class="d-flex align-items-center gap-2 pb-1">
            <a href="#${isMaterial ? 'CU-28' : (isGlosario ? 'CU-32' : (isEval ? 'CU-58' : (isPool ? 'CU-54' : 'CU-66')))}" class="wf-btn wf-btn-sm wf-btn-primary d-flex align-items-center gap-1">
              ${icons.plus()}
              <span>Nuevo ${isMaterial ? 'Material' : (isGlosario ? 'Término' : (isEval ? 'Autoevaluación' : (isPool ? 'Pool' : 'Elemento')))}</span>
            </a>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>
      </div>

      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Título / Recurso</th>
              <th>Tipo</th>
              <th>Pertenencia</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>${isMaterial ? 'Ley 26.831 de Mercado de Capitales' : (isGlosario ? 'TIR (Tasa Interna de Retorno)' : (isEval ? 'Autoevaluación Unidad 1' : 'Pool Unidad 1'))}</strong></td>
              <td>${isMaterial ? 'PDF' : (isGlosario ? 'Definición' : (isEval ? '10 preguntas' : '25 preguntas'))}</td>
              <td>Unidad 1: Marco Regulatorio</td>
              <td><span class="wf-badge status-active">Publicado</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline">${icons.pencilSquare()}</button>
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger">${icons.trash()}</button>
                  <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- TYPE 5: MODAL DE AGREGAR CONTENIDO --- CU-28, CU-32, CU-36, CU-40, CU-54, CU-58, CU-66
  if (['CU-28', 'CU-32', 'CU-36', 'CU-40', 'CU-54', 'CU-58', 'CU-66'].includes(id)) {
    const isMaterial = id === 'CU-28';
    const isGlosario = id === 'CU-32';
    const isForo = id === 'CU-36' || id === 'CU-40';
    const isPool = id === 'CU-54';
    const isEval = id === 'CU-58';
    const isLive = id === 'CU-66';

    let selectedTitle = 'Material / Documento PDF';
    let inputLabel = 'Nombre / Título del material';
    let inputValue = 'Guía Teórica de Renta Fija v2.0 (PDF)';

    if (isGlosario) {
      selectedTitle = 'Glosario de Términos';
      inputLabel = 'Término a registrar';
      inputValue = 'TIR (Tasa Interna de Retorno)';
    } else if (isForo) {
      selectedTitle = 'Foro de Consultas';
      inputLabel = 'Asunto del tema o consulta';
      inputValue = 'Consulta sobre Cálculo de TIR';
    } else if (isEval) {
      selectedTitle = 'Autoevaluación';
      inputLabel = 'Nombre de la autoevaluación';
      inputValue = 'Autoevaluación Unidad 1: Marco Legal';
    } else if (isPool) {
      selectedTitle = 'Pool de Preguntas';
      inputLabel = 'Nombre del pool';
      inputValue = 'Pool Unidad 1: Mercado de Capitales';
    } else if (isLive) {
      selectedTitle = 'Clase en Vivo (Streaming)';
      inputLabel = 'Título de la transmisión';
      inputValue = 'Clase en Vivo #1: Resolución de Prácticos';
    }

    return `
      <div class="wf-modal-box" style="max-width: 900px; margin: 20px auto; border: 1px solid #CBD5E1; border-radius: 12px; box-shadow: 0 24px 54px rgba(0,0,0,0.18); overflow: hidden; background: #FFFFFF;">
        <div class="d-flex justify-content-between align-items-center px-5 py-4 bg-white border-bottom">
          <h3 style="font-size: 19px; font-weight: 700; color: #1E293B; margin: 0;">Añade una actividad o un recurso</h3>
          <span class="wf-close-btn" style="font-size: 22px; color: #94A3B8; cursor: pointer; font-weight: 700;">✕</span>
        </div>

        <div class="row g-0 bg-white" style="min-height: 440px;">
          <div class="col-md-5 border-end p-4" style="background: #F8FAFC;">
            <div class="small fw-bold text-muted text-uppercase mb-3">ACTIVIDADES & RECURSOS</div>
            <div class="d-flex flex-column gap-2">
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isMaterial ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isMaterial ? 'checked' : ''}>
                ${icons.documentText("w-5 h-5 text-primary")}
                <span>Material / Documento PDF</span>
              </label>
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isGlosario ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isGlosario ? 'checked' : ''}>
                ${icons.bookOpen("w-5 h-5 text-warning")}
                <span>Glosario de Términos</span>
              </label>
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isEval ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isEval ? 'checked' : ''}>
                ${icons.clipboardCheck("w-5 h-5 text-success")}
                <span>Autoevaluación</span>
              </label>
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isPool ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isPool ? 'checked' : ''}>
                ${icons.queueList("w-5 h-5 text-info")}
                <span>Pool de Preguntas</span>
              </label>
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isForo ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isForo ? 'checked' : ''}>
                ${icons.chatBubble("w-5 h-5 text-muted")}
                <span>Foro de Consultas</span>
              </label>
              <label class="d-flex align-items-center gap-3 p-3 rounded cursor-pointer ${isLive ? 'bg-white border shadow-sm fw-bold' : ''}">
                <input type="radio" name="act_opt" ${isLive ? 'checked' : ''}>
                ${icons.videoCamera("w-5 h-5 text-danger")}
                <span>Clase en Vivo (Streaming)</span>
              </label>
            </div>
            <div class="mt-4 pt-2 text-end pe-2">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <div class="col-md-7 d-flex flex-column justify-content-between p-4" style="padding: 32px 36px !important;">
            <div>
              <h4 style="font-size: 17px; font-weight: 700; color: #1E293B; margin-bottom: 10px;">${selectedTitle}</h4>
              <div class="mb-4">
                <label class="wf-label">${inputLabel}</label>
                <div class="wf-input-wrap">
                  <input type="text" class="wf-input" value="${inputValue}">
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </div>

              ${isMaterial ? `
                <div class="mb-4">
                  <label class="wf-label">Archivo adjunto o URL</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="guia_teorica_u1.pdf">
                    <button class="wf-btn wf-btn-outline wf-btn-sm">Examinar...</button>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isGlosario ? `
                <div class="mb-4">
                  <label class="wf-label">Definición conceptual</label>
                  <div class="wf-input-wrap">
                    <textarea class="wf-input" rows="3">Tasa que iguala el valor actual de los flujos de fondos con el precio del bono.</textarea>
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}

              ${isLive ? `
                <div class="mb-4">
                  <label class="wf-label">Fecha y Hora de Transmisión</label>
                  <div class="wf-input-wrap">
                    <input type="text" class="wf-input" value="Jueves 28/08/2026 - 19:00 hs">
                    <span class="pin-badge">${badges[2] || 'C'}</span>
                  </div>
                </div>
              ` : ''}
            </div>

            <div class="d-flex justify-content-end align-items-center gap-3 pt-4 border-top">
              <button class="wf-btn wf-btn-sm" style="background: #E2E8F0; color: #475569;">Cancelar</button>
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm fw-bold text-white" style="background: #F97316;">Agregar</button>
                <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 6: ESTUDIO DE CLON DIGITAL CON IA (HEYGEN) --- CU-76
  if (id === 'CU-76') {
    return `
      <div class="wf-card" style="max-width: 1020px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 8px; background: #0F172A; display: flex; align-items: center; justify-content: center; color: white;">
              ${icons.sparkles("w-6 h-6 text-white")}
            </div>
            <div>
              <h3 style="font-size: 17px; font-weight: 800; color: #0F172A; margin: 0;">Estudio de Clon Digital con IA (HeyGen API)</h3>
              <p class="small text-muted" style="margin: 0;">Configure su avatar hiperrealista y clone su voz para el dictado autónomo de clases.</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary wf-btn-sm d-flex align-items-center gap-2">
              ${icons.cog6Tooth("w-4 h-4")}
              <span>Configurar API HeyGen</span>
            </button>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="row g-4">
          <!-- Columna 1: Captura Facial / Avatar Preview -->
          <div class="col-md-6">
            <div class="p-3 border rounded bg-white h-100 d-flex flex-column justify-content-between shadow-sm">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h4 style="font-size: 14px; font-weight: 700; color: #0F172A; margin: 0;">1. Captura de Rostro & Avatar</h4>
                  <span class="wf-badge status-active">Cámara HD Activa</span>
                </div>
                
                <!-- Preview del Avatar / Cámara con Tracking -->
                <div style="height: 220px; background: #0F172A; border-radius: 6px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #334155;">
                  <!-- Avatar Wireframe Silhouette -->
                  <div style="text-align: center; color: #F8FAFC; z-index: 2;">
                    <div style="width: 80px; height: 80px; border-radius: 50%; background: #1E293B; margin: 0 auto 8px; display: flex; align-items: center; justify-content: center; border: 2px solid #64748B; box-shadow: 0 4px 12px rgba(0,0,0,0.3);">
                      ${icons.user("w-10 h-10 text-slate-300")}
                    </div>
                    <div style="font-size: 13px; font-weight: 700;">Lic. Fausto Spotorno</div>
                    <div style="font-size: 10px; color: #94A3B8;">Modelo: Instant Avatar v2 • 1080p 60fps</div>
                  </div>

                  <!-- Face Tracking Bounding Box Grid -->
                  <div style="position: absolute; width: 120px; height: 130px; border: 1.5px dashed #38BDF8; border-radius: 8px; pointer-events: none; opacity: 0.6;"></div>
                  
                  <div style="position: absolute; top: 10px; left: 10px; display: flex; gap: 6px;">
                    <span style="background: rgba(15,23,42,0.85); color: #38BDF8; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; border: 1px solid rgba(56,189,248,0.3);">REC 1080P</span>
                    <span style="background: rgba(15,23,42,0.85); color: #34D399; font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; border: 1px solid rgba(52,211,153,0.3);">● TRACKING ON</span>
                  </div>

                  <div style="position: absolute; bottom: 8px; right: 8px; font-size: 10px; color: #94A3B8; background: rgba(0,0,0,0.6); padding: 2px 6px; border-radius: 3px;">
                    Iluminación: 98% (Óptima)
                  </div>
                </div>
              </div>

              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="small text-muted">Adjuntar archivo o tomar foto web:</span>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline d-flex align-items-center gap-1">
                    ${icons.camera("w-4 h-4")} <span>Tomar Foto</span>
                  </button>
                  <span class="pin-badge">${badges[1] || 'B'}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Columna 2: Calibración y Muestra de Voz -->
          <div class="col-md-6">
            <div class="p-3 border rounded bg-white h-100 d-flex flex-column justify-content-between shadow-sm">
              <div>
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h4 style="font-size: 14px; font-weight: 700; color: #0F172A; margin: 0;">2. Muestra y Clonación de Voz</h4>
                  <span class="wf-badge status-active">Micrófono 48kHz</span>
                </div>

                <div class="p-3 bg-light rounded border mb-3">
                  <div class="small text-muted mb-1 fw-bold">Texto de Calibración Sugerido:</div>
                  <p class="small text-secondary" style="font-style: italic; line-height: 1.4; margin: 0;">
                    "Bienvenidos a la cátedra de Mercado de Capitales. Hoy analizaremos la curva de rendimiento y la duración modificada de los bonos soberanos."
                  </p>
                </div>

                <!-- Visualizador de Espectro de Audio -->
                <div style="height: 90px; background: #0F172A; border-radius: 6px; display: flex; align-items: center; justify-content: center; gap: 5px; padding: 0 20px; border: 1.5px solid #334155; position: relative;">
                  <div style="width: 4px; height: 24px; background: #64748B; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 42px; background: #94A3B8; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 68px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 38px; background: #94A3B8; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 56px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 28px; background: #64748B; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 48px; background: #CBD5E1; border-radius: 2px;"></div>
                  <div style="width: 4px; height: 20px; background: #64748B; border-radius: 2px;"></div>
                  
                  <div style="position: absolute; bottom: 6px; left: 10px; font-size: 10px; color: #94A3B8;">
                    Ruido ambiente: -52 dB (Bajo)
                  </div>
                </div>
              </div>

              <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                <span class="small text-muted">Duración grabada: 00:32 / 00:30 min</span>
                <div class="d-flex align-items-center gap-2">
                  <button class="wf-btn wf-btn-sm wf-btn-outline text-danger d-flex align-items-center gap-1">
                    ${icons.microphone("w-4 h-4")} <span>Grabar Muestra</span>
                  </button>
                  <span class="pin-badge">${badges[2] || 'C'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Consentimiento y Envío a HeyGen API -->
        <div class="mt-4 p-3 bg-light border rounded d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-2">
            <input type="checkbox" id="terms" checked style="width: 16px; height: 16px;">
            <label for="terms" class="small text-muted mb-0">Acepto los términos y condiciones de consentimiento biométrico para síntesis de voz y avatar en HeyGen API.</label>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2">
              ${icons.sparkles("w-4 h-4")}
              <span>Crear Clon en HeyGen</span>
            </button>
            <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 7: GENERAR / MODIFICAR CLASE CON CLON IA --- CU-77, CU-78, CU-79
  if (['CU-77', 'CU-78', 'CU-79'].includes(id)) {
    return `
      <div class="wf-card" style="max-width: 1020px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 8px; background: #0F172A; display: flex; align-items: center; justify-content: center; color: white;">
              ${icons.videoCamera("w-6 h-6 text-white")}
            </div>
            <div>
              <h3 style="font-size: 17px; font-weight: 800; color: #0F172A; margin: 0;">Generador de Clase Audiovisual con Clon IA</h3>
              <p class="small text-muted" style="margin: 0;">Escriba el guión académico o genere contenido para renderizar el video explicativo.</p>
            </div>
          </div>
          <span class="wf-badge status-active">Clon Activo: Fausto Spotorno HD</span>
        </div>

        <div class="row g-4">
          <div class="col-md-7">
            <div class="mb-3">
              <label class="wf-label">Título de la Clase / Video</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Explicación Teórica: Duración Modificada y Convexidad en Bonos">
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            </div>

            <div class="mb-3">
              <label class="wf-label">Unidad Académica de Pertenencia</label>
              <div class="wf-input-wrap">
                <select class="wf-input">
                  <option>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</option>
                </select>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>

            <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="wf-label mb-0">Guión Académico de Locución (Speech Text)</label>
                <a href="#CU-74" class="small text-decoration-none d-flex align-items-center gap-1" style="color: #0F172A; font-weight: 700;">
                  ${icons.sparkles("w-3.5 h-3.5")} Autogenerar guión con IA
                </a>
              </div>
              <div class="wf-input-wrap">
                <textarea class="wf-input" rows="6">En esta clase abordaremos el concepto de modified duration. Cuando la tasa de interés se incrementa, el precio de los títulos cae en proporción inversa a su duración ponderada. Analizaremos la aproximación por series de Taylor...</textarea>
                <span class="pin-badge">${badges[2] || 'C'}</span>
              </div>
            </div>
          </div>

          <div class="col-md-5">
            <div class="p-3 border rounded bg-white shadow-sm mb-3">
              <div class="small fw-bold text-muted text-uppercase mb-2">Previsualización de Render</div>
              <div style="height: 180px; background: #0F172A; border-radius: 6px; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1.5px solid #334155;">
                <div style="text-align: center; color: white;">
                  <div style="width: 56px; height: 56px; border-radius: 50%; background: #1E293B; margin: 0 auto 6px; display: flex; align-items: center; justify-content: center; font-weight: 700; border: 2px solid #64748B;">
                    ${icons.user("w-7 h-7 text-slate-300")}
                  </div>
                  <div style="font-size: 11px; font-weight: 700;">Avatar HeyGen v2.0</div>
                  <div style="font-size: 9px; color: #94A3B8;">Lic. Fausto Spotorno</div>
                </div>
                <div style="position: absolute; top: 8px; left: 8px; background: #DC2626; color: white; padding: 2px 6px; border-radius: 3px; font-size: 9px; font-weight: 700; letter-spacing: 0.5px;">
                  PREVIEW
                </div>
                <div style="position: absolute; bottom: 8px; right: 8px; font-size: 10px; color: #34D399; background: rgba(0,0,0,0.6); padding: 2px 6px; border-radius: 3px;">
                  1080p 60fps
                </div>
              </div>
              <div class="d-flex justify-content-between small text-muted mt-2">
                <span>Tiempo estimado: ~3 min 40s</span>
                <span>Voz: Fausto_ES_AR_v1</span>
              </div>
            </div>

            <div class="d-flex flex-column gap-2">
              <div class="d-flex justify-content-between align-items-center p-2 border rounded bg-light">
                <span class="small text-muted">Voz seleccionada:</span>
                <span class="small fw-bold">Fausto_ES_AR_v1</span>
              </div>
              <div class="d-flex justify-content-between align-items-center p-2 border rounded bg-light">
                <span class="small text-muted">Fondo de pantalla:</span>
                <span class="small fw-bold">Oficina Virtual FCEQyN</span>
              </div>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
          <button class="wf-btn wf-btn-outline">Guardar como Borrador</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2">
              ${icons.sparkles("w-4 h-4")}
              <span>Renderizar Video con Clon</span>
            </button>
            <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 8: GENERACIÓN IA DE BANCO, RESUMEN Y SLIDES --- CU-73, CU-74, CU-75
  if (['CU-73', 'CU-74', 'CU-75'].includes(id)) {
    const isBank = id === 'CU-73';
    const isSummary = id === 'CU-74';
    const isSlides = id === 'CU-75';

    return `
      <div class="wf-card" style="max-width: 900px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div class="d-flex align-items-center gap-3">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: linear-gradient(135deg, #059669, #10B981); display: flex; align-items: center; justify-content: center; color: white;">
              ${icons.sparkles("w-6 h-6")}
            </div>
            <div>
              <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">${isBank ? 'Generador de Banco de Preguntas con IA' : (isSummary ? 'Generador de Resumen Académico con IA' : 'Generador de Presentación de Diapositivas (Slidev/Gamma)')}</h3>
              <p class="small text-muted" style="margin: 0;">Procesamiento automático sobre los materiales teóricos y guías subidas a la unidad.</p>
            </div>
          </div>
          <span class="wf-badge status-active">Modelo: Claude 3.5 Sonnet / Gemini Pro</span>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <label class="wf-label">Unidad Académica de Origen</label>
            <div class="wf-input-wrap">
              <select class="wf-input">
                <option>Unidad 2: Instrumentos de Renta Fija (Bonos y Obligaciones Negociables)</option>
              </select>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Materiales de Referencia a Procesar</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" value="guia_teorica_u2.pdf, ley_26831.pdf" disabled class="bg-disabled">
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="p-3 border rounded bg-light mb-4">
          <div class="small fw-bold text-muted text-uppercase mb-2">Parámetros de Generación</div>
          <div class="row g-3">
            <div class="col-md-4">
              <label class="small text-muted">Nivel de Profundidad:</label>
              <select class="wf-input wf-btn-sm"><option>Avanzado (Examen CNV)</option><option>Intermedio</option></select>
            </div>
            <div class="col-md-4">
              <label class="small text-muted">${isBank ? 'Cantidad de Preguntas:' : (isSlides ? 'Cantidad de Diapositivas:' : 'Extensión del Resumen:')}</label>
              <input type="text" class="wf-input wf-btn-sm" value="${isBank ? '20 preguntas cerradas' : (isSlides ? '12 diapositivas' : '3 páginas síntesis')}">
            </div>
            <div class="col-md-4">
              <label class="small text-muted">Idioma & Formato:</label>
              <input type="text" class="wf-input wf-btn-sm" value="Español (Rioplatense - Financiero)" disabled>
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary d-flex align-items-center gap-2" style="background: #059669;">
              ${icons.sparkles("w-4 h-4")}
              <span>${isBank ? 'Generar Banco de Preguntas' : (isSummary ? 'Generar Resumen' : 'Generar Presentación')}</span>
            </button>
            <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 9: CLASE EN VIVO (STREAMING) --- CU-70, CU-71, CU-72
  if (['CU-70', 'CU-71', 'CU-72'].includes(id)) {
    const isStart = id === 'CU-70';
    const isEnd = id === 'CU-71';
    const isStudent = id === 'CU-72';

    return `
      <div class="wf-card" style="max-width: 1050px; margin: 0 auto; background: #0F172A; color: #FFFFFF; border-color: #1E293B;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-3 border-bottom border-secondary">
          <div class="d-flex align-items-center gap-3">
            <span class="wf-badge" style="background: #DC2626; color: white; animation: pulse 2s infinite;">● EN VIVO</span>
            <div>
              <h3 style="font-size: 16px; font-weight: 700; color: #FFFFFF; margin: 0;">Clase Magistral: Resolución de Prácticos de Renta Fija</h3>
              <p class="small text-muted" style="margin: 0; color: #94A3B8;">Docente: Lic. Fausto Spotorno | Alumnos Conectados: 48</p>
            </div>
          </div>
          <div class="d-flex align-items-center gap-3">
            ${isStart ? `
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm wf-btn-primary" style="background: #2563EB;">Copiar Clave OBS</button>
                <span class="pin-badge">${badges[badges.length - 1] || 'B'}</span>
              </div>
            ` : ''}
            ${isEnd ? `
              <div class="d-flex align-items-center gap-2">
                <button class="wf-btn wf-btn-sm wf-btn-danger">Finalizar Transmisión</button>
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            ` : ''}
            ${isStudent ? `
              <div class="d-flex align-items-center gap-2">
                <span class="wf-badge status-active">Conectado a la Sala</span>
                <span class="pin-badge">${badges[0] || 'A'}</span>
              </div>
            ` : ''}
          </div>
        </div>

        <div class="row g-3">
          <div class="col-md-8">
            <div style="height: 380px; background: #020617; border-radius: 8px; display: flex; align-items: center; justify-content: center; position: relative; border: 1px solid #334155;">
              <div class="text-center text-muted">
                <div style="width: 64px; height: 64px; border-radius: 50%; background: #1E293B; margin: 0 auto 10px; display: flex; align-items: center; justify-content: center;">
                  ${icons.signal("w-8 h-8 text-white")}
                </div>
                <div class="small fw-bold text-light">Transmisión RTMP / WebRTC en Directo (1080p 60fps)</div>
                <div style="font-size: 11px; color: #64748B;">Latencia ultra-baja: 420 ms</div>
              </div>
              <div style="position: absolute; bottom: 12px; left: 12px; display: flex; gap: 8px;">
                <span class="badge bg-dark border border-secondary text-light">Micro: ON</span>
                <span class="badge bg-dark border border-secondary text-light">Cámara: ON</span>
                <span class="badge bg-dark border border-secondary text-light">Grabando automático</span>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div style="height: 380px; background: #1E293B; border-radius: 8px; display: flex; flex-direction: column; border: 1px solid #334155;">
              <div class="p-3 border-bottom border-secondary small fw-bold text-light d-flex justify-content-between">
                <span>Chat en Vivo</span>
                <span class="text-muted">48 participantes</span>
              </div>
              <div class="p-3 flex-1 overflow-y-auto small" style="font-size: 12px; display: flex; flex-direction: column; gap: 8px; color: #CBD5E1;">
                <div><strong class="text-info">Joaquín Küster:</strong> ¿La modified duration incluye cupones corridos?</div>
                <div><strong class="text-warning">Docente:</strong> Sí Joaquín, se calcula sobre el precio dirty.</div>
                <div><strong class="text-info">María Benítez:</strong> ¡Excelente explicación profe!</div>
              </div>
              <div class="p-2 border-top border-secondary">
                <input type="text" class="wf-input" placeholder="Escribir mensaje en el chat..." style="background: #0F172A; border-color: #475569; color: white;">
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 10: AUTOEVALUACIÓN ACTIVA (EXAMEN) --- CU-63
  if (id === 'CU-63') {
    return `
      <div class="wf-card" style="max-width: 900px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">Autoevaluación Unidad 2: Renta Fija y Bonos</h3>
            <p class="small text-muted" style="margin: 0;">Intento 1 de 3 • Tiempo restante: <strong class="text-danger">18:45 min</strong></p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">En progreso</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <!-- Pregunta 1 -->
        <div class="p-4 border rounded mb-4 bg-white shadow-sm">
          <div class="d-flex justify-content-between mb-2">
            <span class="small fw-bold text-muted">PREGUNTA 1 DE 10</span>
            <span class="small text-muted">Valor: 1.00 punto</span>
          </div>
          <p class="fw-bold mb-3" style="font-size: 14px; color: #1E293B;">
            Si la tasa interna de retorno (TIR) de un bono soberano a tasa fija sube 100 puntos básicos, ¿qué sucede con su precio de mercado?
          </p>

          <div class="d-flex flex-column gap-2">
            <label class="d-flex align-items-center gap-3 p-2 rounded border bg-light cursor-pointer">
              <input type="radio" name="p1" checked>
              <span>El precio cae, en una magnitud aproximada a su Modified Duration.</span>
            </label>
            <label class="d-flex align-items-center gap-3 p-2 rounded border cursor-pointer">
              <input type="radio" name="p1">
              <span>El precio sube proporcionalmente al plazo de madurez.</span>
            </label>
            <label class="d-flex align-items-center gap-3 p-2 rounded border cursor-pointer">
              <input type="radio" name="p1">
              <span>El precio se mantiene inalterado ya que el cupón es fijo.</span>
            </label>
          </div>
          <div class="mt-2 text-end">
            <span class="pin-badge">${badges[1] || 'B'}</span>
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Guardar y Continuar Luego</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary" style="background: #059669;">Terminar Intento y Enviar Todo</button>
            <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 11: PAGOS E INSCRIPCIONES --- CU-44, CU-46, CU-47
  if (['CU-44', 'CU-46', 'CU-47'].includes(id)) {
    return `
      <div class="wf-card" style="max-width: 800px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">Pasarela de Pago & Matrícula</h3>
            <p class="small text-muted" style="margin: 0;">Inscripción al curso: <strong>Especialización en Idoneidad Bursátil (Cohorte 2026-1)</strong></p>
          </div>
          <span class="wf-badge status-active">Arancel: $120.000 ARS</span>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <label class="wf-label">Método de Pago Seleccionado</label>
            <div class="wf-input-wrap">
              <div class="wf-input d-flex align-items-center justify-content-between">
                <span>${icons.creditCard("w-4 h-4 me-2")} Tarjeta de Débito / Crédito</span>
                <span class="text-success fw-bold">Activo</span>
              </div>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-6">
            <label class="wf-label">Cupón de Descuento (Opcional)</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Ingresar código...">
              <button class="wf-btn wf-btn-sm wf-btn-outline">Aplicar</button>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="p-3 border rounded bg-light mb-4">
          <div class="d-flex justify-content-between mb-2"><span>Arancel del Curso:</span><strong>$120.000 ARS</strong></div>
          <div class="d-flex justify-content-between mb-2 text-success"><span>Descuento Aplicado:</span><strong>-$0 ARS</strong></div>
          <div class="d-flex justify-content-between border-top pt-2" style="font-size: 16px;"><span>Total a Pagar:</span><strong class="text-navy">$120.000 ARS</strong></div>
        </div>

        <div class="d-flex justify-content-end align-items-center gap-3 pt-3 border-top">
          <button class="wf-btn wf-btn-outline">Cancelar</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-primary" style="background: #059669;">Confirmar y Pagar</button>
            <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 12: ESTADÍSTICAS, INFORMES & AUDITORÍA --- CU-95, CU-96, CU-97, CU-98
  if (['CU-95', 'CU-96', 'CU-97', 'CU-98'].includes(id)) {
    const isAudit = id === 'CU-95';
    const isStats = id === 'CU-98';

    return `
      <div class="wf-card" style="max-width: 1100px; margin: 0 auto;">
        <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
          <div>
            <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">${isAudit ? 'Registro de Auditoría del Sistema' : 'Tablero Analítico & Estadísticas Clave'}</h3>
            <p class="small text-muted" style="margin: 0;">Supervisión en tiempo real de ingresos, métricas de retención y logs de eventos.</p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="wf-badge status-active">Actualizado al Instante</span>
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        ${!isAudit ? `
          <!-- KPI Cards -->
          <div class="row g-3 mb-4">
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Total Alumnos Matriculados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #0F172A; margin: 4px 0;">1,420</h2>
                <div class="small text-success">↑ +14% este mes</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Ingresos Facturados</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #059669; margin: 4px 0;">$17.4M ARS</h2>
                <div class="small text-success">↑ +22% vs trimestre anterior</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Tasa de Aprobación</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #2563EB; margin: 4px 0;">88.5%</h2>
                <div class="small text-muted">Promedio en autoevaluaciones</div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="p-3 border rounded bg-light">
                <div class="small text-muted">Clases Clon Generadas</div>
                <h2 style="font-size: 24px; font-weight: 800; color: #7C3AED; margin: 4px 0;">64</h2>
                <div class="small text-muted">HeyGen AI Studio</div>
              </div>
            </div>
          </div>
        ` : ''}

        <div class="wf-table-wrap">
          <table class="wf-table">
            <thead>
              <tr>
                <th>${isAudit ? 'Timestamp' : 'Curso / Programa'}</th>
                <th>${isAudit ? 'Usuario / Actor' : 'Cohorte'}</th>
                <th>${isAudit ? 'Acción / Evento' : 'Alumnos Activos'}</th>
                <th>${isAudit ? 'Módulo Afectado' : 'Facturación'}</th>
                <th class="text-end">Detalle</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><strong>${isAudit ? '2026-08-26 07:45:12' : 'Especialización en Idoneidad Bursátil'}</strong></td>
                <td>${isAudit ? 'Lic. Fausto Spotorno (Docente)' : '2026-1 (En curso)'}</td>
                <td>${isAudit ? 'Crear Clon de IA (voice_id: #v92)' : '840 inscriptos'}</td>
                <td>${isAudit ? 'MOD-F-06: IA' : '$10.080.000 ARS'}</td>
                <td class="text-end">
                  <div class="d-inline-flex align-items-center gap-1">
                    <button class="wf-btn wf-btn-sm wf-btn-outline">Ver Registro</button>
                    <span class="pin-badge">${badges[1] || badges[badges.length - 1] || 'B'}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 13: CUENTA / PERFIL / LOGIN / RECUPERO --- CU-81, CU-86, CU-87, CU-90, CU-92
  if (['CU-81', 'CU-86', 'CU-87', 'CU-90', 'CU-92'].includes(id)) {
    const isLogin = id === 'CU-90';
    const isRegister = id === 'CU-81';
    const isRecovery = id === 'CU-92';
    const isProfile = id === 'CU-86' || id === 'CU-87';

    if (isLogin || isRegister || isRecovery) {
      return `
        <div class="wf-card" style="max-width: 480px; margin: 40px auto; box-shadow: 0 12px 32px rgba(0,0,0,0.08);">
          <div class="text-center mb-4">
            <h3 style="font-size: 20px; font-weight: 800; color: var(--wf-navy-dark);">${isLogin ? 'Iniciar Sesión en Idóneos Online' : (isRegister ? 'Crear Cuenta de Usuario' : 'Recuperar Contraseña')}</h3>
            <p class="small text-muted">${isLogin ? 'Ingrese sus credenciales académicas para acceder' : (isRegister ? 'Complete sus datos para registrarse en la plataforma' : 'Le enviaremos un enlace de restablecimiento seguro')}</p>
          </div>

          <div class="mb-3">
            <label class="wf-label">Correo Electrónico Institucional</label>
            <div class="wf-input-wrap">
              <input type="email" class="wf-input" value="${isLogin ? 'fausto.spotorno@idoneos.online' : 'usuario@correo.com'}">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          ${!isRecovery ? `
            <div class="mb-3">
              <label class="wf-label">Contraseña</label>
              <div class="wf-input-wrap">
                <input type="password" class="wf-input" value="••••••••••••">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
          ` : ''}

          <div class="d-flex flex-column gap-2 mt-4">
            <div class="d-flex align-items-center gap-2 w-100">
              <button class="wf-btn wf-btn-primary w-100">${isLogin ? 'Iniciar Sesión' : (isRegister ? 'Registrarme' : 'Enviar Enlace')}</button>
              <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
            </div>
            
            ${isLogin ? `
              <div class="text-center my-2 text-muted small">o continúe con</div>
              <div class="d-flex align-items-center gap-2 w-100">
                <button class="wf-btn wf-btn-outline w-100">Continuar con Google</button>
                <span class="pin-badge">${badges[3] || 'D'}</span>
              </div>
            ` : ''}
          </div>
        </div>
      `;
    }

    if (isProfile) {
      return `
        <div class="wf-card" style="max-width: 800px; margin: 0 auto;">
          <div class="d-flex justify-content-between align-items-center pb-3 mb-4 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <div class="user-avatar-circle" style="width: 52px; height: 52px; font-size: 18px;">FS</div>
              <div>
                <h3 style="font-size: 18px; font-weight: 700; color: #0F172A; margin: 0;">Perfil de Usuario: Lic. Fausto Spotorno</h3>
                <p class="small text-muted" style="margin: 0;">Docente Titular • Mercado de Capitales & Finanzas</p>
              </div>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-outline wf-btn-sm">Editar Perfil</button>
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="wf-label">Nombre Completo</label>
              <div class="wf-input-wrap">
                <input type="text" class="wf-input" value="Fausto Spotorno">
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
            </div>
            <div class="col-md-6">
              <label class="wf-label">Correo Electrónico</label>
              <input type="email" class="wf-input" value="fausto.spotorno@idoneos.online" disabled class="bg-disabled">
            </div>
            <div class="col-md-6">
              <label class="wf-label">Estado de la Cuenta</label>
              <input type="text" class="wf-input" value="Activo y Habilitado para Dictado" disabled class="bg-disabled">
            </div>
            <div class="col-md-6">
              <label class="wf-label">Clon Digital (HeyGen)</label>
              <input type="text" class="wf-input" value="Vinculado (#avatar_spotorno_v2)" disabled class="bg-disabled">
            </div>
          </div>

          <div class="d-flex justify-content-end align-items-center gap-3 pt-3 mt-4 border-top">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary">Guardar Cambios</button>
              <span class="pin-badge">${badges[2] || badges[badges.length - 1] || 'C'}</span>
            </div>
          </div>
        </div>
      `;
    }
  }

  // --- SPECIALIZED 14: BAJA / ELIMINACIÓN GENÉRICA ---
  const isDelete = name.toLowerCase().includes('baja') || name.toLowerCase().includes('cancelar') || name.toLowerCase().includes('eliminar') || name.toLowerCase().includes('quitar');
  if (isDelete) {
    return `
      <div class="wf-card" style="max-width: 600px; margin: 40px auto;">
        <div class="wf-card-header text-center mb-4">
          <div class="wf-icon-danger mb-3">${icons.exclamationTriangle("w-8 h-8 text-danger")}</div>
          <h3>Confirmar Baja / Eliminación</h3>
          <p class="text-muted">¿Está seguro de que desea realizar la operación sobre el elemento seleccionado?</p>
        </div>

        <div class="wf-card p-3 mb-4 bg-light">
          <div class="d-flex justify-content-between mb-2">
            <span class="text-muted">Elemento a dar de baja:</span>
            <strong>Registro Seleccionado #${id.replace('CU-', '')} (${name})</strong>
          </div>
          <div class="d-flex justify-content-between">
            <span class="text-muted">Estado actual:</span>
            <span class="wf-badge status-active">Activo / Vigente</span>
          </div>
        </div>

        <div class="d-flex justify-content-end gap-3">
          <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
          <div class="d-flex align-items-center gap-2">
            <button class="wf-btn wf-btn-danger">${icons.trash()} Confirmar Baja</button>
            <span class="pin-badge">${badges[badges.length - 1] || 'A'}</span>
          </div>
        </div>
      </div>
    `;
  }

  // --- SPECIALIZED 15: BÚSQUEDAS / TABLAS DE GESTIÓN ---
  const isSearch = name.toLowerCase().startsWith('buscar') || name.toLowerCase().startsWith('consultar') || name.toLowerCase().startsWith('ver') || name.toLowerCase().startsWith('explorar');
  if (isSearch) {
    return `
      <div class="wf-card mb-4">
        <div class="row align-items-end">
          <div class="col-md-5">
            <label class="wf-label">Criterio de búsqueda / Filtro principal</label>
            <div class="wf-input-wrap">
              <input type="text" class="wf-input" placeholder="Buscar en ${name}...">
              <span class="pin-badge">${badges[0] || 'A'}</span>
            </div>
          </div>
          <div class="col-md-4">
            <label class="wf-label">Filtro Secundario / Estado</label>
            <div class="wf-select-container">
              <div class="wf-input-wrap">
                <div class="wf-input wf-select-trigger">
                  <span>Todos los registros</span>
                  ${icons.chevronDown()}
                </div>
                <span class="pin-badge">${badges[1] || 'B'}</span>
              </div>
              <div class="wf-dropdown-menu">
                <div class="wf-dropdown-item active">☑ Todos los registros</div>
                <div class="wf-dropdown-item">☐ Activos / Vigentes</div>
                <div class="wf-dropdown-item">☐ Históricos</div>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="d-flex align-items-center gap-2">
              <button class="wf-btn wf-btn-primary w-100">Buscar</button>
              <span class="pin-badge">${badges[2] || 'C'}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="wf-table-wrap">
        <table class="wf-table">
          <thead>
            <tr>
              <th>Identificador / Nombre</th>
              <th>Detalle / Contexto</th>
              <th>Registro</th>
              <th>Estado</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>Elemento Principal #${id.replace('CU-', '')}A</strong></td>
              <td>Mercado de Capitales & Finanzas</td>
              <td>2026-08-25</td>
              <td><span class="wf-badge status-active">Activo</span></td>
              <td class="text-end">
                <div class="d-inline-flex align-items-center gap-1">
                  <button class="wf-btn wf-btn-sm wf-btn-outline">Seleccionar / Ver</button>
                  <span class="pin-badge">${badges[3] || badges[badges.length - 1] || 'D'}</span>
                </div>
              </td>
            </tr>
            <tr>
              <td><strong>Elemento Secundario #${id.replace('CU-', '')}B</strong></td>
              <td>Valuación & Finanzas Corporativas</td>
              <td>2026-08-20</td>
              <td><span class="wf-badge status-active">Activo</span></td>
              <td class="text-end">
                <button class="wf-btn wf-btn-sm wf-btn-outline">Seleccionar / Ver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    `;
  }

  // --- SPECIALIZED 16: FORMULARIOS DE REGISTRO / MODIFICACIÓN ESPECÍFICOS ---
  return `
    <div class="wf-card" style="max-width: 860px; margin: 0 auto;">
      <div class="wf-card-header mb-4 pb-3 border-bottom">
        <h3 style="font-size: 18px; font-weight: 700; color: #0F172A;">${name}</h3>
        <p class="text-muted" style="margin: 0; font-size: 13px;">Complete los datos correspondientes en el sistema.</p>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="wf-label">Nombre / Denominación</label>
          <div class="wf-input-wrap">
            <input type="text" class="wf-input" value="${name.includes('curso') ? 'Especialización en Idoneidad Bursátil' : (name.includes('categoría') ? 'Mercado de Capitales' : (name.includes('cohorte') ? 'Cohorte 2026-1' : 'Registro de ' + name))}">
            <span class="pin-badge">${badges[0] || 'A'}</span>
          </div>
        </div>

        <div class="col-md-6">
          <label class="wf-label">Categoría / Asociación</label>
          <div class="wf-select-container">
            <div class="wf-input-wrap">
              <div class="wf-input wf-select-trigger">
                <span>Mercado de Capitales</span>
                ${icons.chevronDown()}
              </div>
              <span class="pin-badge">${badges[1] || 'B'}</span>
            </div>
          </div>
        </div>

        <div class="col-12">
          <label class="wf-label">Descripción Académica / Contenido</label>
          <div class="wf-input-wrap">
            <textarea class="wf-input" rows="3">Descripción detallada correspondiente al caso de uso ${id} (${name}).</textarea>
            <span class="pin-badge">${badges[2] || 'C'}</span>
          </div>
        </div>

        <div class="col-md-4">
          <label class="wf-label">Precio / Arancel</label>
          <input type="text" class="wf-input" value="$120.000 ARS">
        </div>

        <div class="col-md-4">
          <label class="wf-label">Estado</label>
          <input type="text" class="wf-input" value="Habilitado / Activo" disabled class="bg-disabled">
        </div>

        <div class="col-md-4">
          <label class="wf-label">Docente Responsable</label>
          <input type="text" class="wf-input" value="Lic. Fausto Spotorno" disabled class="bg-disabled">
        </div>
      </div>

      <div class="wf-card-footer d-flex justify-content-end gap-3 mt-4 pt-3 border-top">
        <button class="wf-btn wf-btn-outline">Cancelar / Volver</button>
        <div class="d-flex align-items-center gap-2">
          <button class="wf-btn wf-btn-primary">Confirmar y Guardar</button>
          <span class="pin-badge">${badges[badges.length - 1] || 'D'}</span>
        </div>
      </div>
    </div>
  `;
}

// 6. Build Complete HTML Page with User Profile Floating Dropdown Menu
let html = `<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Wireframes UI — Idóneos Online (${cus.length} Casos de Uso Reales)</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        /* ==========================================================================
           ESTILO OFICIAL DEFINITIVO — IDÓNEOS ONLINE
           Paleta: Deep Navy (#081426, #0F233D), Acento Oro (#D4A03D, #C59030), Blanco (#FFFFFF)
           Bordes suaves, tarjetas con pill badges de categoría/aranceles y botones dorados.
           ========================================================================== */
        :root {
            --wf-navy-dark: #081426;
            --wf-navy-header: #071324;
            --wf-navy-card-top: #132A4A;
            --wf-gold: #D4A03D;
            --wf-gold-hover: #C59030;
            --wf-gold-light: #FEF3C7;
            --wf-gold-text: #92400E;
            --wf-bg: #F4F6F9;
            --wf-canvas: #E9ECEF;
            --wf-border: #DDE2E8;
            --wf-text: #0F172A;
            --wf-text-muted: #64748B;
            --wf-surface: #FFFFFF;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            -webkit-font-smoothing: antialiased;
        }

        body {
            background-color: var(--wf-canvas);
            color: var(--wf-text);
            display: flex;
            height: 100vh;
            overflow: hidden;
        }

        svg {
            display: inline-block;
            vertical-align: middle;
            flex-shrink: 0;
        }
        .w-3 { width: 14px; }
        .h-3 { height: 14px; }
        .w-3\.5 { width: 15px; }
        .h-3\.5 { height: 15px; }
        .w-4 { width: 16px; }
        .h-4 { height: 16px; }
        .w-5 { width: 20px; }
        .h-5 { height: 20px; }
        .w-6 { width: 24px; }
        .h-6 { height: 24px; }
        .w-7 { width: 28px; }
        .h-7 { height: 28px; }
        .w-8 { width: 32px; }
        .h-8 { height: 32px; }
        .w-10 { width: 40px; }
        .h-10 { height: 40px; }

        /* Navegación Lateral */
        #nav-sidebar {
            width: 310px;
            background: #081426;
            color: #FFFFFF;
            overflow-y: auto;
            border-right: 1px solid #142844;
            display: flex;
            flex-direction: column;
            flex-shrink: 0;
        }

        .nav-header {
            padding: 20px;
            border-bottom: 1px solid #142844;
            background: #050E1B;
        }

        .nav-header h2 {
            font-size: 14px;
            font-weight: 800;
            letter-spacing: 0.5px;
            color: #FFFFFF;
        }

        .nav-header p {
            font-size: 11px;
            color: #94A3B8;
            margin-top: 3px;
        }

        .nav-search {
            padding: 12px 16px;
            border-bottom: 1px solid #142844;
        }

        .nav-search input {
            width: 100%;
            padding: 8px 12px;
            border-radius: 6px;
            border: 1px solid #1E3A5F;
            background: #0F233D;
            color: #FFFFFF;
            font-size: 12px;
            outline: none;
            transition: all 0.15s;
        }

        .nav-search input:focus {
            border-color: var(--wf-gold);
        }

        .module-group {
            border-bottom: 1px solid #142844;
        }

        .module-header {
            padding: 10px 16px;
            background: #0D2038;
            font-size: 11px;
            font-weight: 700;
            color: var(--wf-gold);
            letter-spacing: 0.3px;
            border-left: 3px solid var(--wf-gold);
        }

        .module-list {
            list-style: none;
        }

        .nav-item a {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 8px 16px;
            color: #94A3B8;
            text-decoration: none;
            font-size: 12px;
            transition: all 0.15s;
        }

        .nav-item a:hover {
            background: #132A4A;
            color: #FFFFFF;
            padding-left: 20px;
        }

        .nav-item .cu-tag {
            font-size: 10px;
            font-weight: 700;
            padding: 2px 6px;
            border-radius: 4px;
            background: rgba(255,255,255,0.08);
            color: #E2E8F0;
            border: 1px solid rgba(255,255,255,0.1);
        }

        /* Viewport Central (Canvas de Figma) */
        #viewport {
            flex: 1;
            overflow-y: auto;
            padding: 40px;
            display: flex;
            flex-direction: column;
            gap: 64px;
            align-items: center;
        }

        .figure-wrapper {
            width: 100%;
            max-width: 1160px;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .figure-caption {
            font-size: 13px;
            font-weight: 600;
            color: #475569;
            text-align: left;
            padding-left: 2px;
        }

        /* Marco de Pantalla / Screen Frame Oficial */
        .screen-frame {
            width: 100%;
            min-height: 680px;
            background: #F8FAFC;
            border-radius: 8px;
            border: 1px solid #CBD5E1;
            box-shadow: 0 10px 30px rgba(8, 20, 38, 0.08);
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        /* Top Navbar Oficial (Navy Header) */
        .wf-top-navbar {
            height: 58px;
            background: var(--wf-navy-header);
            padding: 0 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: relative;
            z-index: 100;
            border-bottom: 1px solid #142844;
        }

        .wf-brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .wf-brand-logo {
            font-size: 16px;
            font-weight: 800;
            color: #FFFFFF;
            letter-spacing: -0.3px;
        }

        .wf-brand-logo span {
            color: var(--wf-gold);
        }

        .wf-brand-sub {
            font-size: 9px;
            font-weight: 700;
            letter-spacing: 0.8px;
            color: #94A3B8;
            text-transform: uppercase;
        }

        .wf-top-nav-links {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .wf-top-nav-link {
            color: #CBD5E1;
            font-size: 12px;
            font-weight: 600;
            text-decoration: none;
            transition: color 0.15s;
        }

        .wf-top-nav-link:hover {
            color: #FFFFFF;
        }

        .wf-top-nav-pill {
            padding: 5px 12px;
            border-radius: 20px;
            border: 1px solid rgba(212, 160, 61, 0.4);
            color: var(--wf-gold);
            font-size: 11px;
            font-weight: 700;
            background: rgba(212, 160, 61, 0.08);
        }

        .wf-user-trigger-pill {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 3px 12px 3px 4px;
            border-radius: 20px;
            border: 1px solid #1E3A5F;
            background: #0F233D;
            cursor: pointer;
            color: #FFFFFF;
        }

        .user-avatar-circle {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: var(--wf-gold);
            color: #081426;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 800;
        }

        /* Hero Banner del Cuerpo Docente */
        .wf-hero-banner {
            background: var(--wf-navy-header);
            padding: 24px 32px 28px;
            color: #FFFFFF;
            border-bottom: 1px solid #142844;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }

        .wf-hero-tag {
            font-size: 10px;
            font-weight: 800;
            color: var(--wf-gold);
            letter-spacing: 0.8px;
            text-transform: uppercase;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .wf-hero-title {
            font-size: 22px;
            font-weight: 800;
            color: #FFFFFF;
            letter-spacing: -0.3px;
        }

        .wf-hero-desc {
            font-size: 12px;
            color: #94A3B8;
            margin-top: 2px;
        }

        .wf-btn-gold {
            background: var(--wf-gold);
            color: #081426;
            font-size: 12px;
            font-weight: 700;
            padding: 8px 18px;
            border-radius: 20px;
            border: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(212, 160, 61, 0.25);
            transition: all 0.15s;
            text-decoration: none;
        }

        .wf-btn-gold:hover {
            background: var(--wf-gold-hover);
        }

        /* Main Content */
        .wf-body {
            flex: 1;
            display: flex;
            flex-direction: column;
            background: #F4F6F9;
            position: relative;
        }

        .wf-main-content {
            flex: 1;
            padding: 28px 32px;
            overflow-y: auto;
        }

        /* Footer Inferior Oficial */
        .wf-screen-footer {
            height: 48px;
            background: var(--wf-navy-header);
            border-top: 1px solid #142844;
            padding: 0 28px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #94A3B8;
        }

        .wf-screen-footer strong {
            color: #FFFFFF;
        }

        .wf-screen-footer strong span {
            color: var(--wf-gold);
        }

        /* Tarjetas de Cursos Oficiales (Estilo Imagen) */
        .wf-cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
        }

        .wf-course-card {
            background: #FFFFFF;
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid #E2E8F0;
            box-shadow: 0 6px 16px rgba(8, 20, 38, 0.05);
            display: flex;
            flex-direction: column;
            transition: transform 0.15s, box-shadow 0.15s;
        }

        .wf-course-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 24px rgba(8, 20, 38, 0.08);
        }

        .wf-course-card-thumb {
            height: 120px;
            background: var(--wf-navy-card-top);
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 12px;
        }

        .wf-course-thumb-icon {
            color: rgba(255,255,255,0.25);
            margin-bottom: 8px;
        }

        .wf-course-pills-row {
            display: flex;
            align-items: center;
            gap: 6px;
            width: 100%;
            justify-content: flex-start;
        }

        .wf-pill-tag {
            background: var(--wf-gold);
            color: #081426;
            font-size: 9px;
            font-weight: 800;
            padding: 3px 8px;
            border-radius: 12px;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }

        .wf-pill-status {
            background: var(--wf-gold);
            color: #081426;
            font-size: 9px;
            font-weight: 800;
            padding: 3px 8px;
            border-radius: 12px;
            letter-spacing: 0.3px;
        }

        .wf-course-card-body {
            padding: 18px 20px;
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .wf-course-title {
            font-size: 15px;
            font-weight: 800;
            color: #081426;
            margin-bottom: 6px;
            line-height: 1.3;
        }

        .wf-course-desc {
            font-size: 11px;
            color: #64748B;
            margin-bottom: 14px;
            line-height: 1.4;
            flex: 1;
        }

        .wf-course-info-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #64748B;
            font-weight: 600;
            padding-bottom: 12px;
            border-bottom: 1px solid #F1F5F9;
        }

        .wf-course-price {
            font-size: 13px;
            font-weight: 800;
            color: #081426;
        }

        .wf-course-card-footer {
            padding: 12px 20px 16px;
            background: #FFFFFF;
        }

        .wf-btn-manage-course {
            width: 100%;
            background: var(--wf-gold);
            color: #081426;
            font-size: 12px;
            font-weight: 700;
            padding: 9px 0;
            border-radius: 8px;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            cursor: pointer;
            transition: background 0.15s;
            text-decoration: none;
        }

        .wf-btn-manage-course:hover {
            background: var(--wf-gold-hover);
        }

        /* Pin Badges */
        .pin-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 26px;
            height: 26px;
            min-width: 26px;
            border-radius: 50%;
            background: #081426;
            color: #FFFFFF;
            font-size: 12px;
            font-weight: 800;
            border: 2px solid var(--wf-gold);
            box-shadow: 0 2px 6px rgba(8, 20, 38, 0.3);
            flex-shrink: 0;
            user-select: none;
        }

        /* Inputs y Formularios Oficiales */
        .wf-card {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 10px;
            padding: 24px;
            box-shadow: 0 4px 12px rgba(8, 20, 38, 0.03);
        }

        .wf-label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            color: #334155;
            text-transform: uppercase;
            letter-spacing: 0.3px;
            margin-bottom: 6px;
        }

        .wf-input {
            width: 100%;
            height: 40px;
            padding: 8px 14px;
            border: 1.5px solid var(--wf-border);
            border-radius: 6px;
            font-size: 13px;
            color: #081426;
            outline: none;
            background: #FFFFFF;
            transition: border-color 0.15s;
        }

        .wf-input:focus {
            border-color: #081426;
        }

        textarea.wf-input {
            height: auto;
        }

        .bg-disabled {
            background: #F8FAFC !important;
            color: #64748B !important;
            border-style: dashed !important;
        }

        .wf-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            height: 40px;
            padding: 0 18px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
            cursor: pointer;
            border: 1.5px solid transparent;
            text-decoration: none;
            white-space: nowrap;
            transition: all 0.15s;
        }

        .wf-btn-primary {
            background: #081426;
            color: #FFFFFF;
            border-color: #081426;
        }

        .wf-btn-primary:hover {
            background: #102540;
        }

        .wf-btn-outline {
            background: #FFFFFF;
            border-color: #CBD5E1;
            color: #334155;
        }

        .wf-btn-outline:hover {
            background: #F8FAFC;
            border-color: #081426;
            color: #081426;
        }

        .wf-btn-danger {
            background: #DC2626;
            border-color: #DC2626;
            color: #FFFFFF;
        }

        .wf-btn-sm {
            height: 32px;
            padding: 0 12px;
            font-size: 11px;
        }

        .wf-link {
            font-size: 13px;
            color: #081426;
            font-weight: 700;
            text-decoration: underline;
            text-underline-offset: 2px;
            cursor: pointer;
        }

        /* Tablas */
        .wf-table-wrap {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
        }

        .wf-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 12px;
        }

        .wf-table th, .wf-table td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--wf-border);
            text-align: left;
            vertical-align: middle;
        }

        .wf-table th {
            background: #F8FAFC;
            font-weight: 700;
            color: #475569;
            font-size: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* Badges de Estado */
        .wf-badge {
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 4px;
            border: 1px solid #CBD5E1;
            text-transform: uppercase;
            letter-spacing: 0.3px;
        }

        .status-active {
            background: #FEF3C7;
            color: #92400E;
            border-color: #FCD34D;
        }

        .status-inactive {
            background: #FFFFFF;
            color: #94A3B8;
            border-color: #E2E8F0;
        }

        .wf-icon-danger {
            width: 48px;
            height: 48px;
            background: #FEE2E2;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto;
        }

        /* Meta-Strip Superior */
        .meta-strip {
            padding: 8px 16px;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: #64748B;
        }

        .meta-strip strong {
            color: #081426;
        }

        .meta-strip code {
            background: #F1F5F9;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            color: #081426;
            border: 1px solid #E2E8F0;
        }

        /* Dropdown Flotante */
        .wf-user-menu-wrapper {
            position: relative;
        }

        .wf-user-floating-dropdown {
            position: absolute;
            top: 44px;
            right: 0;
            width: 250px;
            background: #FFFFFF;
            border: 1px solid #CBD5E1;
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
            z-index: 200;
            overflow: hidden;
            display: none;
            flex-direction: column;
        }

        .wf-dropdown-user-header {
            padding: 12px 14px;
            background: #F8FAFC;
            border-bottom: 1px solid #CBD5E1;
        }

        .wf-dropdown-user-name {
            font-size: 12px;
            font-weight: 700;
            color: #081426;
        }

        .wf-dropdown-user-role {
            font-size: 10px;
            font-weight: 700;
            color: var(--wf-gold);
            text-transform: uppercase;
        }

        .wf-dropdown-user-email {
            font-size: 11px;
            color: #64748B;
            margin-top: 2px;
        }

        .wf-dropdown-section {
            padding: 6px 8px;
            border-bottom: 1px solid #F1F5F9;
        }

        .wf-dropdown-section:last-child {
            border-bottom: none;
        }

        .wf-dropdown-section-title {
            font-size: 9px;
            font-weight: 700;
            color: #94A3B8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 2px;
            padding-left: 6px;
        }

        .wf-dropdown-item-btn {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 5px 8px;
            font-size: 11px;
            color: #334155;
            border-radius: 4px;
            text-decoration: none;
            transition: all 0.1s;
        }

        .wf-dropdown-item-btn:hover {
            background: #F1F5F9;
            color: #081426;
        }

        .wf-dropdown-item-btn.text-danger {
            color: #DC2626;
            font-weight: 600;
        }

        .wf-dropdown-editing-toggle-box {
            background: #F8FAFC;
            border: 1px solid #CBD5E1;
            border-radius: 4px;
            padding: 5px 8px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .wf-tab-btn {
            padding: 8px 14px;
            font-size: 12px;
            font-weight: 600;
            color: #64748B;
            background: transparent;
            border: none;
            border-bottom: 2px solid transparent;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
        }

        .wf-tab-btn.active {
            color: #081426;
            border-bottom-color: var(--wf-gold);
            font-weight: 800;
        }

        .wf-unit-box {
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 8px;
            overflow: hidden;
        }

        .wf-unit-header {
            padding: 14px 18px;
            background: #F8FAFC;
            border-bottom: 1px solid var(--wf-border);
        }

        .wf-unit-body {
            padding: 18px;
        }

        .wf-content-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 10px;
        }

        .wf-subcontent-title {
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
            color: #64748B;
            margin-bottom: 6px;
        }

        .wf-input-wrap {
            display: flex;
            align-items: center;
            gap: 8px;
            width: 100%;
        }

        .wf-input-wrap input, .wf-input-wrap textarea, .wf-input-wrap .wf-select-trigger, .wf-input-wrap select {
            flex: 1;
        }

        .wf-select-container {
            position: relative;
            width: 100%;
        }

        .wf-select-trigger {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #FFFFFF;
            cursor: pointer;
        }

        .wf-dropdown-menu {
            position: absolute;
            top: 44px;
            left: 0;
            width: 100%;
            background: #FFFFFF;
            border: 1px solid var(--wf-border);
            border-radius: 6px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
            z-index: 50;
            overflow: hidden;
        }

        .wf-dropdown-item {
            padding: 8px 12px;
            font-size: 12px;
            color: #334155;
            border-bottom: 1px solid #F1F5F9;
            cursor: pointer;
        }

        .wf-dropdown-item.active {
            background: #FEF3C7;
            font-weight: 700;
            color: #92400E;
        }
    </style>
</head>
<body>
    <div id="nav-sidebar">
        <div class="nav-header">
            <h2>IDÓNEOS ONLINE</h2>
            <p>${cus.length} Wireframes de Casos de Uso</p>
        </div>
        <div class="nav-search">
            <input type="text" id="searchInput" placeholder="Buscar CU o nombre..." oninput="filterCUs()">
        </div>
        <div id="moduleList">
`;

modules.forEach(m => {
  html += `
            <div class="module-group">
                <div class="module-header">${m.name}</div>
                <ul class="module-list">
  `;
  m.cus.forEach(c => {
    html += `
                    <li class="nav-item">
                        <a href="#${c.id}">
                            <span>${c.name}</span>
                            <span class="cu-tag">${c.id}</span>
                        </a>
                    </li>
    `;
  });
  html += `
                </ul>
            </div>
  `;
});

html += `
        </div>
    </div>

    <div id="viewport">
`;

let figNumber = 1;
cus.forEach(cu => {
  const roleInfo = getRoleInfo(cu.actors, cu.id);
  const dssMessage = dssMap[cu.id] || 'Operación de sistema correspondiente';

  html += `
        <div class="figure-wrapper" id="${cu.id}">
            <div class="meta-strip">
                <div>
                    <span><strong>CU Real:</strong> ${cu.id}: ${cu.name}</span>
                    <span style="margin: 0 8px; color: #CBD5E1;">•</span>
                    <span><strong>Actor(es):</strong> ${cu.actors}</span>
                </div>
                <div>
                    <span><strong>DSS:</strong> <code>${dssMessage}</code></span>
                </div>
            </div>

            <div class="figure-caption">
                <strong>Figura ${figNumber++}.</strong> Prototipo UI / Wireframe para <em>${cu.name}</em> (${cu.id}).
            </div>

            <div class="screen-frame">
                <!-- Top Navbar Oficial -->
                <div class="wf-top-navbar">
                    <div class="wf-brand">
                        <div class="d-flex flex-column">
                            <div class="wf-brand-logo">📈 Idóneos <span>Online</span></div>
                            <div class="wf-brand-sub">FINANZAS • ECONOMÍA • MERCADO DE CAPITALES</div>
                        </div>
                    </div>

                    <div class="wf-top-nav-links">
                        <a href="#CU-06" class="wf-top-nav-link">Inicio</a>
                        <a href="#CU-06" class="wf-top-nav-link">Catálogo de Cursos</a>
                        <span class="wf-top-nav-pill">${roleInfo.isAdmin ? 'Panel Administrador' : (roleInfo.isDocente ? 'Panel Docente' : 'Panel Alumno')}</span>
                        <a href="#CU-99" class="wf-top-nav-link">Acerca de</a>

                        <div class="wf-user-menu-wrapper">
                            <div class="wf-user-trigger-pill" onclick="toggleUserDropdown(this)">
                                <div class="user-avatar-circle">${roleInfo.initials}</div>
                                <span style="font-size: 11px; font-weight: 700;">${roleInfo.name}</span>
                                ${icons.chevronDown("w-3 h-3 text-white ms-1")}
                            </div>

                            <div class="wf-user-floating-dropdown">
                                <div class="wf-dropdown-user-header">
                                    <div class="wf-dropdown-user-name">${roleInfo.name}</div>
                                    <div class="wf-dropdown-user-role">${roleInfo.role}</div>
                                    <div class="wf-dropdown-user-email">${roleInfo.email}</div>
                                </div>
  `;

  roleInfo.dropdownSections.forEach(sec => {
    html += `
                                <div class="wf-dropdown-section">
                                    <div class="wf-dropdown-section-title">${sec.title}</div>
    `;
    if (sec.hasEditingToggle) {
      html += `
                                    <div class="wf-dropdown-editing-toggle-box">
                                        <span class="small fw-bold">${sec.isEditingActive ? 'Modo Edición Activado' : 'Activar Edición'}</span>
                                        <span class="wf-badge ${sec.isEditingActive ? 'status-active' : 'status-inactive'}">${sec.isEditingActive ? 'ON' : 'OFF'}</span>
                                    </div>
      `;
    }
    if (sec.items) {
      sec.items.forEach(it => {
        html += `
                                    <a href="#${it.cu}" class="wf-dropdown-item-btn ${it.isDanger ? 'text-danger' : ''}">
                                        <span>${it.label}</span>
                                        <span class="small text-muted">${it.cu}</span>
                                    </a>
        `;
      });
    }
    html += `
                                </div>
    `;
  });

  html += `
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Hero Banner Contextual Superior (Navy) -->
                <div class="wf-hero-banner">
                    <div>
                        <div class="wf-hero-tag">
                            <span>🎓 CUERPO DOCENTE • IDÓNEOS ONLINE</span>
                        </div>
                        <h2 class="wf-hero-title">${cu.id === 'CU-01' ? 'Mis Cursos Asignados' : cu.name}</h2>
                        <p class="wf-hero-desc">Bienvenido/a, ${roleInfo.name}</p>
                    </div>
                    ${roleInfo.isDocente || roleInfo.isAdmin ? `
                    <div>
                        <a href="#CU-03" class="wf-btn-gold">
                            ${icons.plus("w-4 h-4")}
                            <span>Crear Curso</span>
                        </a>
                    </div>
                    ` : ''}
                </div>

                <div class="wf-body">
                    <div class="wf-main-content">
                        ${generateScreenContent(cu)}
                    </div>
                </div>

                <!-- Footer Oficial -->
                <div class="wf-screen-footer">
                    <div>
                        <strong>Idóneos <span>Online</span> S.A.S.</strong> • Plataforma de Educación Financiera, Economía & Mercado de Capitales
                    </div>
                    <div>
                        🔒 FCEQyN — UNaM • Proyecto Software (LSI) / Trabajo Final (ASI)
                    </div>
                </div>
            </div>
        </div>
  `;
});

html += `
    </div>

    <script>
        function filterCUs() {
            const query = document.getElementById('searchInput').value.toLowerCase();
            const groups = document.querySelectorAll('.module-group');
            
            groups.forEach(group => {
                const items = group.querySelectorAll('.nav-item');
                let hasVisible = false;
                
                items.forEach(item => {
                    const text = item.textContent.toLowerCase();
                    if (text.includes(query)) {
                        item.style.display = 'block';
                        hasVisible = true;
                    } else {
                        item.style.display = 'none';
                    }
                });
                
                group.style.display = hasVisible ? 'block' : 'none';
            });
        }

        function toggleUserDropdown(triggerEl) {
            const wrapper = triggerEl.closest('.wf-user-menu-wrapper');
            if (wrapper) {
                const dropdown = wrapper.querySelector('.wf-user-floating-dropdown');
                if (dropdown) {
                    if (dropdown.style.display === 'none') {
                        dropdown.style.display = 'flex';
                    } else {
                        dropdown.style.display = 'none';
                    }
                }
            }
        }
    </script>
</body>
</html>
`;

// 7. Write output file
const outputPath = path.join(__dirname, 'docs', 'diseño', 'Pantallas_CU_Reales.html');
fs.writeFileSync(outputPath, html, 'utf8');

console.log(`Successfully generated specialized wireframes in ${outputPath}`);
