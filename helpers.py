import cv2
import numpy as np
from sklearn.cluster import KMeans

def get_grass_color(img):

    # Convert image to HSV color space
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    # Define range of green color in HSV
    lower_green = np.array([30, 40, 40])
    upper_green = np.array([80, 255, 255])

    # Threshold the HSV image to get only green colors
    mask = cv2.inRange(hsv, lower_green, upper_green)

    # Calculate the mean value of the pixels that are not masked
    masked_img = cv2.bitwise_and(img, img, mask=mask)
    grass_color = cv2.mean(img, mask=mask)

    return grass_color[:3]
    #la variable grass_color est un tuple représentant la valeur BGR
    #Cela signifie que si vous appelez la fonction get_grass_color sur une image,
    #vous obtiendrez un tuple contenant la couleur moyenne de l'herbe dans l'image au format BGR
def get_players_boxes(result):

    players_imgs = []
    players_boxes = []

    for box in result.boxes:

        label = int(box.cls.cpu().numpy()[0])

        if label == 0:
            x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
            player_img = result.orig_img[y1: y2, x1: x2]
            players_imgs.append(player_img)
            players_boxes.append(box)

    return players_imgs, players_boxes
#players_imgs: Une liste d'images (np.array) représentant les valeurs BGR des parties de l'image qui contiennent des joueurs.
#players_boxes: Une liste d'objets Boxes qui contiennent diverses informations sur les boîtes englobantes des joueurs trouvées dans l'image.
def get_kits_colors(players, grass_hsv=None, frame=None):

    kits_colors = []

    if grass_hsv is None:
        grass_color = get_grass_color(frame)
        grass_hsv = cv2.cvtColor(np.uint8([[list(grass_color)]]), cv2.COLOR_BGR2HSV)

    for player_img in players:
        # Convert image to HSV color space
        hsv = cv2.cvtColor(player_img, cv2.COLOR_BGR2HSV)

        # Define range of green color in HSV
        lower_green = np.array([grass_hsv[0, 0, 0] - 10, 40, 40])
        upper_green = np.array([grass_hsv[0, 0, 0] + 10, 255, 255])

        # Threshold the HSV image to get only green colors
        mask = cv2.inRange(hsv, lower_green, upper_green)

        # Bitwise-AND mask and original image
        mask = cv2.bitwise_not(mask)
        upper_mask = np.zeros(player_img.shape[:2], np.uint8)
        upper_mask[0:player_img.shape[0]//2, 0:player_img.shape[1]] = 255
        mask = cv2.bitwise_and(mask, upper_mask)

        kit_color = np.array(cv2.mean(player_img, mask=mask)[:3])
        kits_colors.append(kit_color)

    return kits_colors
def get_kits_classifier(kits_colors):

    kits_kmeans = KMeans(n_clusters=3,n_init=20)
    kits_kmeans.fit(kits_colors)
    return kits_kmeans
def classify_kits(kits_classifier, kits_colors):

    team = kits_classifier.predict(kits_colors)

    return team
def classify_person(team,x1,x):

        if team == 0:
            return "Player1"
        elif team == 1:
            return "Player2"
        else:
            if (x1==x) :
               return "GK"
            else:
              return "referee"
def get_left_team_label(players_boxes, kits_colors, kits_clf):

    left_team_label = 0
    team_0 = []
    team_1 = []
    personne = []
    for i in range(len(players_boxes)):
        x1, y1, x2, y2 = map(int, players_boxes[i].xyxy[0].cpu().numpy())
        team = classify_kits(kits_clf, [kits_colors[i]]).item()

        if team == 0:
            team_0.append(np.array([x1]))
        elif team == 1:
            team_1.append(np.array([x1]))
        else:
            personne.append(np.array([x1]))
    team_0 = np.array(team_0)
    team_1 = np.array(team_1)

    if np.average(team_0) - np.average(team_1) > 0:
        left_team_label = 1

    return left_team_label
def get_GK2(players_boxes):
    L = []
    for i in range(len(players_boxes)):
        x1, y1, x2, y2 = map(int, players_boxes[i].xyxy[0].cpu().numpy())
        L.append(np.array([x1]))
    XGK2=max(L)
    return XGK2
