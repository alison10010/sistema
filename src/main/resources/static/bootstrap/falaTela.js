// falaTela.js — TTS local (OFFLINE-ONLY) priorizando VOZ FEMININA pt-BR

// Nomes femininos comuns em pt-BR (Windows/macOS/Android). Ajuste se precisar.
const NOMES_FEMININOS = [
  "maria", "luciana", "francisca", "hélia", "helia", "ana", "camila", "fernanda",
  "letícia", "leticia", "helena", "juliana", "marina", "sophia", "sofia"
];

// Alguns nomes masculinos para despriorizar
const NOMES_MASCULINOS = ["daniel", "ricardo", "thiago", "tiago", "joão", "joao"];

let vozLocal = null;

function isPtBr(v) {
  return (v.lang || "").toLowerCase().startsWith("pt-br");
}

function pareceLocal(v) {
  // Chrome/Edge: localService=true para voz offline do sistema
  if (v.localService === true) return true;
  // Fallback: nomes típicos de vozes de sistema offline
  const nome = (v.name || "").toLowerCase();
  return nome.includes("microsoft") || nome.includes("luciana");
}

// Heurística de ranking: pt-BR + local + nome feminino > outros
function scoreVoz(v) {
  let s = 0;
  if (isPtBr(v)) s += 5;
  if (pareceLocal(v)) s += 5;

  const nome = (v.name || "").toLowerCase();

  // bônus forte se nome parecer feminino
  if (NOMES_FEMININOS.some(n => nome.includes(n))) s += 5;

  // pequena penalidade se nome parecer masculino
  if (NOMES_MASCULINOS.some(n => nome.includes(n))) s -= 3;

  // bônus extra para “Microsoft ...” (comum no Windows e offline)
  if (nome.includes("microsoft")) s += 2;

  return s;
}

function escolherVozLocalPtBr() {
  const voices = speechSynthesis.getVoices() || [];
  if (!voices.length) return null;

  // Filtra só pt-BR locais (ou que pareçam locais) e ranqueia
  const candidatas = voices
    .filter(v => isPtBr(v) && pareceLocal(v))
    .sort((a, b) => scoreVoz(b) - scoreVoz(a));

  if (candidatas.length) return candidatas[0];

  // Se não achou, tenta qualquer local (último recurso — ainda offline)
  const locais = voices
    .filter(v => pareceLocal(v))
    .sort((a, b) => scoreVoz(b) - scoreVoz(a));

  return locais[0] || null;
}

function initVozLocal() {
  const tentar = () => {
    vozLocal = escolherVozLocalPtBr();
    if (!vozLocal) {
      console.warn("[TTS] Nenhuma voz local pt-BR encontrada (ou carregada). Instale/ative uma voz feminina pt-BR no sistema.");
    } else {
      console.log(`[TTS] Usando voz local: ${vozLocal.name} (${vozLocal.lang}) | localService=${vozLocal.localService === true}`);
    }
  };

  tentar();
  if (typeof speechSynthesis.onvoiceschanged !== "undefined") {
    speechSynthesis.onvoiceschanged = tentar;
  }
}

// ===== API pública: falar senha/guichê (somente se houver voz local) =====
async function falarSenhaGuiche(prefixo, senha, guiche) {
  try { if (speechSynthesis.speaking) speechSynthesis.cancel(); } catch {}

  if (!vozLocal) {
    console.error("[TTS] Sem voz local pt-BR. Não vou falar (offline-only).");
    return;
  }

  // Mais lenta e levemente mais aguda (soando mais feminina e clara)
  const frase = `Senha ${prefixo || ""}${senha} Guichê ${guiche}`;
  const u = new SpeechSynthesisUtterance(frase);
  u.lang = "pt-BR";
  u.voice = vozLocal;

  // ↓ ajuste de velocidade e tom
  u.rate = 1.0;   // 1.0 é padrão; 0.9 é ~10% mais lento
  u.pitch = 1.05; // leve aumento de pitch para timbre mais feminino

  return new Promise(resolve => {
    u.onend = resolve;
    speechSynthesis.speak(u);
  });
}

// Inicializa ao carregar o script
initVozLocal();

// Disponibiliza globalmente
window.falarSenhaGuiche = falarSenhaGuiche;
