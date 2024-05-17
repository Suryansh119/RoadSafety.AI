#deployed page=https://universe.roboflow.com/ai-ehrr3/testing-pcqgi
from roboflow import Roboflow

rf = Roboflow(api_key="B23RU3MxCJVf0RabBD5c")
project = rf.workspace().project("testing-pcqgi")

project.version(3).deploy(model_type="yolov5", model_path=f"./Model/best.pt")