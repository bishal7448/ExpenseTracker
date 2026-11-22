import json
from flask import Flask, request, jsonify

app = Flask(__name__)
try:
    app.config.from_pyfile("config.py")
except Exception:
    pass

_message_service = None

def get_message_service():
    global _message_service
    if _message_service is None:
        from service.message_service import MessageService
        _message_service = MessageService()
    return _message_service

@app.route("/v1/ds/message/", methods=["POST"])
def handle_message():
    message = None
    # 1. Try JSON body
    if request.is_json:
        data = request.get_json(silent=True) or {}
        if isinstance(data, dict):
            message = data.get('message')
    
    # 2. Try form data
    if not message and request.form:
        message = request.form.get('message')
        
    # 3. Try raw text / raw body
    if not message:
        raw_text = request.get_data(as_text=True)
        if raw_text:
            try:
                data = json.loads(raw_text)
                if isinstance(data, dict):
                    message = data.get('message')
            except Exception:
                message = raw_text

    if not message:
        return jsonify({
            "error": "No message provided",
            "hint": "In Postman, send Body -> raw -> JSON: {\"message\": \"...\"} and Header Content-Type: application/json"
        }), 400

    service = get_message_service()
    result = service.process_message(message)
    if result is None:
        return jsonify({"status": "ignored", "message": "Not a recognized bank SMS"}), 200

    if hasattr(result, "model_dump"):
        return jsonify(result.model_dump()), 200
    elif hasattr(result, "dict"):
        return jsonify(result.dict()), 200
    return jsonify(result), 200

if __name__ == "__main__":
    print("ExpenseTracker DsService is ready on http://localhost:8080", flush=True)
    app.run(host="0.0.0.0", port=8080, debug=True, use_reloader=False)
