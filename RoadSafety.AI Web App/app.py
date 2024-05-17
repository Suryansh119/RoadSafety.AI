import streamlit as st
from streamlit_option_menu import option_menu
import cv2
import inference
import json
from streamlit_lottie import st_lottie
from twilio.rest import Client
import requests
def lottie(path:str):
    with open(path,"r") as p:
        return json.load(p)
    
st.set_page_config(
        page_title="RoadSafety.AI",
        page_icon="🎥",
        layout="wide"
)

path=lottie("main.json")
st_lottie(path,1,False,True,"high",620)


api ="B23RU3MxCJVf0RabBD5c"
project = "testing-pcqgi"
version ="/3" 
names= {0: 'accident',1:'fight',2:"Fall-Detected"}

hide_streamlit_style = """
            <style>
            #MainMenu {visibility: hidden;}
            footer {visibility: hidden;}
                .stDeployButton {
            visibility: hidden;
        }
            </style>
            """
st.markdown(hide_streamlit_style, unsafe_allow_html=True)

with st.sidebar:
    selected=option_menu("RoadSafety.AI",
    ['Accident Detection'],
    icons=['camera-video'],
    default_index=0,
 
    )


if selected == "Accident Detection":
    col1,col2=st.columns(2)
    with col1:
        form = st.form(key="my_form")
        form.title("Ready For Real-Time Analytics")
        camera_name = form.text_input("Camera Name (Optional)", "", key="camera_name",placeholder="NH-47 Highway")
        rtsp_address = form.text_input("RTSP Address of Live Camera", "", key="rtsp_address",placeholder="rtsp://server.example.org:8080/test.sdp")
        submit_button = form.form_submit_button("Submit",use_container_width=True, type='primary')
    with col2:
        st_lottie(lottie("Animation - 1714209681585.json"))
    if submit_button:
     if not rtsp_address:
        with col1:
         st.warning("Please fill in the RTSP address field.")
    if submit_button and rtsp_address:
        st.title(f"Live Camera Feed ({camera_name})")
        
        checklist=False
        model = inference.get_model(project+version,api_key=api)
        cap = cv2.VideoCapture(rtsp_address)
        if not cap.isOpened():
            st.error("Error: Failed to open RTSP stream")
        else:
            frame_container = st.empty()
            stop_button = st.button("Stop",use_container_width=True, type='primary')  
            while True:
               
                if checklist==False:
                    checklist=set()
                ret, frame = cap.read()
                if not ret:
                    st.error("Error: Failed to read frame from RTSP stream")
                    break
                if stop_button:  # If stop button is clicked, break out of the loop
                    break
                prediction=model.infer(frame,confidence=0.7)
                print(prediction)
                for prediction in prediction[0].predictions:  # Accessing the list of predictions within the first ObjectDetectionInferenceResponse
                    x = int(prediction.x)
                    y = int(prediction.y)
                    width = int(prediction.width)
                    height = int(prediction.height)
                    predicted_class=prediction.class_name
                    if checklist is not True:
                        checklist.add(predicted_class)
                    x0 = x - width / 2
                    x1 = x + width / 2
                    y0 = y - height / 2
                    y1 = y + height / 2

                    start_point = (int(x0), int(y0))
                    end_point = (int(x1), int(y1))
            
                    cv2.rectangle(frame, start_point, end_point, color=(0,0,255), thickness=3)
                    if str(type(checklist))=="<class 'set'>":
                        for item in checklist:
                       
                            if item in names.values():
                                
                                    checklist=True
                                    account_sid ="sid"
                                    auth_token = "token"
                                    client = Client(account_sid, auth_token)
                                    name=camera_name
                                    if name:
                                        message = client.messages \
                                                    .create(
                                                        body="Potential Dangerous Condition Happened at "+name,
                                                        from_='+12293744229',
                                                        to='<NUMBER>'
                                                    )
                                    else:
                                         message = client.messages \
                                                    .create(
                                                        body="Potential Dangerous Condition Happened",
                                                        from_='+12293744229',
                                                        to='<NUMBER>'
                                                    )

                                    key = "key"
                                    secret = "secret"
                                    from_number = "+447441421689"
                                    to = "<NUMBER>"
                                    locale = "en-US"
                                    url = "https://calling.api.sinch.com/calling/v1/callouts"

                                    payload = {
                                    "method": "ttsCallout",
                                    "ttsCallout": {
                                        "cli": from_number,
                                        "destination": {
                                        "type": "number",
                                        "endpoint": to
                                        },
                                        "locale": locale,
                                        "text": "Potential Dangerous Condition Happened Start Taking Action"
                                    }
                                    }
                                    headers = {"Content-Type": "application/json"}
                                    response = requests.post(url, json=payload, headers=headers, auth=(key, secret))
                                    data = response.json()
                                    print(data)
                                    st.toast("Surveillance officer was alerted via phone call")
                                    st.toast("Surveillance officer was alerted via sms")
                                    break
                
                frame_container.image(frame, channels="BGR", use_column_width=True)
              