from flask import Flask, request, jsonify
from transformers import AutoModel, AutoTokenizer
from together import Together
import easyocr
from config import API_KEY
import os

app = Flask(__name__)

client = Together(api_key=API_KEY)

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

reader = easyocr.Reader(['en', 'es'], gpu=True)


def process_image_easyocr(image_path):
    if not os.path.exists(image_path):
        print("Error: La imagen proporcionada no existe.")
        return None
    try:
        results = reader.readtext(image_path)
        recognized_text = " ".join([text[1] for text in results])
        return recognized_text
    except Exception as e:
        print(f"Error processing image with EasyOCR: {e}")
        return None

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

        res = model.chat(tokenizer, image_path, ocr_type='ocr')
        print("OCR completed successfully.")
        print(res)

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

    try:
        response = client.images.generate(
            prompt=prompt,
            model="black-forest-labs/FLUX.1-schnell",
            steps=4
        )

        image_url = response.data[0].url
        print("Image URL generated:", image_url)

        return jsonify({"image_url": image_url})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/easyocr', methods=['POST'])
def easyocr_route():
    """Procesa una imagen y devuelve el texto reconocido usando EasyOCR (ruta separada)"""
    if 'image' not in request.files:
        return jsonify({"error": "No image provided"}), 400

    image_file = request.files['image']
    print("Image file received:", image_file.filename)

    image_path = "./temp_image.jpg"
    try:
        image_file.save(image_path)
        print(f"Image saved at {image_path}")

        text = process_image_easyocr(image_path)
        print("EasyOCR completed successfully.")

        os.remove(image_path)
        print(f"Temporary image {image_path} deleted.")

        if text:
            return jsonify({"text": text})
        else:
            return jsonify({"error": "No text found or error in processing"}), 500

    except Exception as e:
        return jsonify({"error": f"Failed to process image: {str(e)}"}), 500



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
