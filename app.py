from flask import Flask, request, jsonify,send_file
from annotation import annotate_video
import os
from werkzeug.utils import secure_filename
from ultralytics import YOLO
import time
import uuid
from multiprocessing import Pool, Process, Manager

app = Flask(__name__)
model = YOLO('yolov8n.pt')

processes = {}

@app.route('/')
def index():
    return 'Welcome to my Flask App'

def postprocess(result):
    print("finished: %s" % result)

@app.route('/upload_video', methods=['POST'])
def upload_video():
    print("post request is received")
    video_file = request.files['video']
    print("file is accessed")

    if video_file:
        tic = time.perf_counter()
        myuuid = str(uuid.uuid4())
        video_name = secure_filename(video_file.filename)
        video_path = os.path.join("C:/Users/User/OneDrive - Ministere de l'Enseignement Superieur et de la Recherche Scientifique/Bureau/appflask/", video_name)  
        print("Start saving video with uuid " + myuuid)
        video_file.save(video_path)
        print("Video is successfully saved")
        p = Process(target=annotate_video, args=([video_path, model, processes, myuuid]))
        p.start()
        print(f"Process took {time.perf_counter() - tic} seconds")
        return jsonify({"uuid": myuuid}) 
    else:
        return 'No video file provided'

@app.route('/annotated_video', methods=['GET'])
def get_annotated_video():
    print("get request is done")
    uiid =request.args.get('uiid')
    print("uiid is " + uiid)
    print("Processes = ")
    print(processes)
    if uiid in processes.keys():
        print("Sending video to client")
        annotated_video_path = processes[uiid]  # Récupérez le chemin de la vidéo annotée à partir du dictionnaire process
        # Return the annotated video file
        return send_file(annotated_video_path, mimetype='video/mp4')
    else:
        print("Video is not available yet")
        return "ERROR" 


if __name__ == "__main__":
    manager = Manager()
    processes = manager.dict()
    app.run(host='0.0.0.0', port=5000,debug=True)

