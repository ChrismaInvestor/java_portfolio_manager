from flask import Flask
import json

app = Flask(__name__)

@app.route('/')
def home():
    return json.dumps({'name': 'alice',
                       'email': 'alice@outlook.com'})

if __name__=='__main__':
    app.run(debug=True)
