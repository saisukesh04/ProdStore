# ProdStore

## Introduction
Gone are the days where people step down every day to their local shops to buy foodstuff that gets consumed the same day itself. Nowadays with the rise of supermarkets and all-in-one stores, people buy in bulk and store the items, to be consumed for the entire 1 or 2 months. The main objective of this project is to overcome the negligence of the people towards food items and avoid wastage due to expiry, by reminding them from time to time about the products that are nearing their expiry date and urging them to consume them at the earliest. This application can also be used by medical and other grocery stores to sell their products before expiry. This gives rise to cases wherein people tend to forget when a particular food (especially packaged) item expires, thus giving rise to wastage of the food items.

## Implementation
  As people are glued to their phones over anything else now-a-days, what’s better than giving them a reminder on their phones itself. Hence, we plan to design an app in which the user can input the products that they buy and their manufacturing date and the app in turn will remind them a week before the product expires.

## Novelty
  While one can use a reminders app for doing the same task, what’s different in this app is that the user need not enter the name of the product or search for its expiry date manually. All that the user has to do is to scan the back of the product where the manufacturing date and barcode is present and the system will automatically make an entry and save it, to remind you in the future.

## Workflow
* User scans the back of the product containing the manufacturing date and barcode using his/her phone camera.
* The system tries to get the manufacturing date from the scanned image, if not asks the user to enter it manually. 
* The scanned barcode is read and searched in a database containing the product name and best before/expiry period of the corresponding barcode.
* This data of a particular product is stored and visible to the user once he/she has scanned the barcode of the product. The user adds as many products as he wishes to using the same procedures. 
* On the homepage of the app, the user is shown the products that are expiring in a week and is also suggested which product he/she can consume for the day.
* If a product is consumed before the app reminds him/her, he/she can conveniently delete the product from the app.
* A week before any product expires, a reminder is sent to the user reminding him of the same.

## Future scope
This app can further be extended to store the monthly expense on food items as it stores all the items scanned by the user and it can also extract the MRP along with the name and manufacturing date. It can also be programmed to include entries of products brought from online e-commerce sites which can be returned within a specific time period, so the app can also remind them about the same if the user wishes to return an item. It can even be used to suggest people items that they can consume everyday from their list so that no item crosses their expiry date. 

## 📚 Tech Stack:
- <code><img height="38" src="https://i1.pngguru.com/preview/736/783/702/macos-app-icons-android-studio-png-icon.jpg"></code> Android Studio
- <code><img height="38" src="https://img.icons8.com/color/452/mongodb.png"></code> MongoDb
- <code><img height="38" src="https://icon2.cleanpng.com/20180417/irq/kisspng-firebase-cloud-messaging-computer-icons-google-clo-github-5ad5d3cde70706.9853526815239628299463.jpg"></code> Firebase
