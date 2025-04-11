from flask import Flask, request, jsonify
import hashlib

app = Flask(__name__)

@app.route('/check', methods=['POST'])
def check_file():
    # Verify that a file was sent in the request
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400

    file = request.files['file']
    expected = request.form.get('expected', '').strip()

    # Read the file's bytes and compute the SHA256 hash
    file_bytes = file.read()
    computed_hash = hashlib.sha256(file_bytes).hexdigest()

    result = {
        'computed_hash': computed_hash,
    }
    if expected:
        result['matches'] = (computed_hash == expected)
    else:
        result['matches'] = None

    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
