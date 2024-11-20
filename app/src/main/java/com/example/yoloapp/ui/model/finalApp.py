from flask import Flask, request, jsonify
from transformers import AutoModel, AutoTokenizer
from together import Together

from config import API_KEY

import os

app = Flask(__name__)

# Inicialización del cliente para la generación de imágenes
client = Together(api_key=API_KEY)

# Cargar el modelo y tokenizador de OCR
tokenizer = AutoTokenizer.from_pretrained('ucaslcl/GOT-OCR2_0', trust_remote_code=True)
model = AutoModel.from_pretrained(
    'ucaslcl/GOT-OCR2_0', 
    trust_remote_code=True, 
    low_cpu_mem_usage=True, 
    device_map='cuda', 
    use_safetensors=True, 
    pad_token_id=tokenizer.eos_token_id
)
model = model.eval().cuda()

@app.route('/ocr', methods=['POST'])
def ocr():
    """Procesa una imagen y devuelve el texto reconocido (OCR)"""
    if 'image' not in request.files:
        return jsonify({"error": "No image provided"}), 400

    image_file = request.files['image']
    print("Image file received:", image_file.filename)

    image_path = "./temp_image.jpg"
    try:
        image_file.save(image_path)
        print(f"Image saved at {image_path}")

    except Exception as e:
        return jsonify({"error": f"Failed to save or open image: {str(e)}"}), 500
    
    try:
        # Realiza OCR con el modelo cargado
        res = model.chat(tokenizer, image_path, ocr_type='ocr')
        print("OCR completed successfully.")
        print(res)

        # Elimina la imagen temporal después de procesarla
        os.remove(image_path)
        print(f"Temporary image {image_path} deleted.")
    
    except Exception as e:
        return jsonify({"error": f"Failed to process image: {str(e)}"}), 500

    return jsonify({"text": res})

@app.route('/generate_image', methods=['POST'])
def generate_image():
    """Genera una imagen a partir de un prompt utilizando Together API"""
    data = request.json
    prompt = data.get("prompt")
    print("Received prompt:", prompt)

    if not prompt:
        return jsonify({"error": "No prompt provided"}), 400

    # Genera la imagen usando Together API
    try:
        response = client.images.generate(
            prompt=prompt,
            model="black-forest-labs/FLUX.1-schnell",
            steps=4
        )
        
        # Obtiene la URL de la imagen generada
        image_url = response.data[0].url
        print("Image URL generated:", image_url)

        return jsonify({"image_url": image_url})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
