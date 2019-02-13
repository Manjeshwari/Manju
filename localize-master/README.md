# Localize

WIFI Localization and path mapping.

## Overview

### Code

The code provides WIFI localization and path mapping. It consists of two files.

- `localize.py`
- `path.py`

Localize analyzes WIFI data to determine where the WIFI data originated.

Pathing analyzes WIFI data to draw paths between WIFI data points creating a 2D map.

### Mobile

The mobile app is collects WIFI data points which are used for WIFI localization.

### Server

The server handles requests to the web app and handles backend processes.

As a web app, it provides a view of the 2D map generated using the data produced by `path.py`. 

### Database

#### Structure

The following is a hierarchal view of how the data is structured in Firebase Real-time database.

```json
{
    "device": {
        "user_id": {
            "location": "location",
            "data": {
                "id_n": 0
            },
            "time": "yy-MM-dd hh:mm:ss UTC"
        }
    },
    "user":{
        "user_id": {
            "location": {
                "location_name": {
                    "sample_id": {
                        "data": {
                            "id_n": 0
                        },
                        "time": "yy-MM-dd hh:mm:ss UTC"
                    }
                }
            }
        }
    }
}
```

#### Formats & Conventions

##### Timestamp Format

Timestamps get formatted as **UTC**.

- `yy-MM-dd hh:mm:ss UTC` 

##### Location Format

Location names are like addresses. 

- Each field is separated with a comma **","**.
- Spaces get replaced with underscores **"_"**.

Example:

- `"Location_Name,Street_No,Street_Name,Street_Type,Floor,Building_Name,Town,Region,Postcode,Country"`

Omitted data gets replaced with an underscore **"_"**, and is comma-separated.

- `"Location_Name,_,_,_,_,Building_Name,_,_,_,_"`

Fields that contain commas are invalid.

- `Location_Name: "Bath,room"`

## Tech Stack

### Hardware

- Android devices supporting Android SDK 26+.

### Software

- Android Studio 3.2.1+

### Programming Languages

- Python
- Java

### Libraries & Frameworks

- Flask
- NumPy
- apscheduler
- firebase_admin

### Services

- Firebase
- Heroku

## Installation

For development and local deployment of the application install the following.

- Python
- JRE: 1.8+
- Android Studio 3.2.1+

Install Python requirements:

```shell
cd path/project
python -m install requirements
```

## Setup

### Setting up Services

You'll need to use services that require an account.

- Create a GitHub account.
- Create a Heroku account.
- Create a Google account.

After creating your GitHub account login to it.

- Clone this repository.

After creating your Heroku account login to it.

- Create an app and give it a name.
- Navigate to the Deploy tab.
- Scroll down to deployment method.
- Click GitHub "Connect to GitHub" and complete the authentication process.
- Lookup and choose the repo you cloned.

After creating your Google account visit https://firebase.google.com  and login to <u>Firebase console</u>.

- Create a project and give it a name.
- Download firebase credentials for that project and drop it in the project directory `c:/path/project/server` renaming it as `firebase-credentials.json`.

### Setting up Android

Startup Android Studio and open the project in `c:/path/project/mobile/android`.

You need to connect the app to the Firebase project you created.

- In Android Studio navigate to `Tools->Firebase`.
- A side-bar will appear on the right.
- Navigate to <u>Real-time Database</u> and click on it then click on <u>Save and retrieve data</u>.
- Follow the instructions they provided to connect to Firebase.

## Deployment

Deploying the project is simple.

- Go to the app on your Heroku account. 
- Navigate to the Deploy tab.
- Scroll down to <u>Manual Deploy</u>.
- Click <u>Deploy Branch</u>.

## Development Notes

### Insights

**Use your favorite search engine to find answers to your questions.**

### Short Comings

Android Studio may import dependencies that result in failed builds. The fix for this is to use an older but compatible version of that dependency. 

Android 9 (Pie) throttles WIFI scans. That slows the process of programmatic WIFI logging or performing WIFI-based localization efficiently. However, it does not appear to affect WIFI logging when manually performed through the UI.

Limitations:

- 4 scans every 2 minutes when the app is active.
- 1 scan every 30 minutes when the app is in the background.