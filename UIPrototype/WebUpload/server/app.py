from flask import Flask, jsonify, request
from flask_cors import CORS
import os

ADMINS = [
    {'name': 'deepsand', 'password': '987654'}
]

DEBUG = True

app = Flask(__name__)
app.config.from_object(__name__)

CORS(app, resources={r'/*': {'origins': '*'}})


@app.route('/verify', methods=['GET', 'POST'])
def verify_admin():

    response_object = {'status': 'fail'}

    if (request.method == 'GET'):
        return jsonify({})

    else:
        post_data = request.get_json()
        login_name = post_data.get('name')
        login_password = post_data.get('password')

        for admin in ADMINS:
            admin_name = admin['name']
            admin_password = admin['password']
            if (login_name == admin_name and login_password == admin_password):
                response_object['status'] = 'success'
                break

        return jsonify(response_object)


if __name__ == '__main__':
    app.run(host=os.getenv('IP', '0.0.0.0'),
            port=int(os.getenv('PORT', 5000)))
