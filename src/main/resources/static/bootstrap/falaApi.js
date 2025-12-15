function falarSenhaGuiche(prefixo, senha, guiche) {
    const texto = `Senha ${prefixo} ${senha}. Guichê ${guiche}.`;
    falarTextoTTS(texto);
}

function falarTextoTTS(texto) {
    if (!texto || !texto.trim()) {
        console.warn("Texto vazio para TTS");
        return;
    }

    // Se quiser, pode mostrar no console para debug
    console.log("Enviando para TTS:", texto);

    fetch("http://172.16.3.39:8081/synthesize", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ text: texto })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Erro HTTP TTS: " + response.status);
        }
        return response.blob();
    })
    .then(blob => {
        console.log("Tamanho do áudio TTS (bytes):", blob.size);
        if (blob.size === 0) {
            console.error("Áudio TTS vazio (0 bytes).");
            return;
        }

        const url = URL.createObjectURL(blob);
        const audioTts = new Audio(url);
		
		audioTts.playbackRate = 0.9;  // 0.9 = 10% mais lento

        audioTts.play().catch(err => {
            console.error("Erro ao reproduzir áudio TTS:", err);
        });

        audioTts.onended = () => {
            URL.revokeObjectURL(url);
        };
    })
    .catch(err => {
        console.error("Erro ao chamar o serviço TTS:", err);
    });
}
