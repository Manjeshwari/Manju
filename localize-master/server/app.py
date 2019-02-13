import json

from flask import Flask, request

app = Flask(__name__)


@app.route("/", methods=["GET"])
def home():
    if request.method == "GET":
        return ("Welcome!", 200)
    else:
        return ("Your shall not pass!", 200)


if __name__ == "__main__":
    app.run(host="0.0.0.0")
