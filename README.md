# Shopify Provider for ShopApp Android

Shopify provider for ShopApp for Android. ShopApp is an application that turns a Shopify-based store into a mobile app. 
ShopApp syncs with Shopify store and transfers a product catalog and user data to a mobile app. 
The app provides features like customizable push notifications, promo codes, and convenient payments with popular digital wallets like Android Pay.

Picture

## About ShopApp Shopify for Android

This library contains the Shopify provider for [ShopApp for Android](https://github.com/rubygarage/shopapp-android). 

ShopApp an open source application that allows store owners of popular ecommerce platforms like Shopify and Magento to launch an iOS or Android mobile app for their store. 
ShopApp connects with the platform and transfers an existing store to a mobile app. 

# Installation
## Connect the library

To use the ShopApp provider for Shopify, connect the library to the [main application](https://github.com/rubygarage/shopapp-android) with Gradle:

``` 
implementation "com.github.rubygarage:shopapp-shopify-android:1.0.0" 
```

## Change the ShopApplication file
Next, change the **app/src/main/java/com/shopapp/ShopApplication.kt** file by adding the following code: 

``` 
//Initialize your api here.
val api = ShopifyApi(this, "SHOP DOMAIN", "API KEY", "ADMIN API KEY", "ADMIN API PASSWORD")
appComponent = buildAppComponent(api, dao) 
```

Where: 
**SHOP DOMAIN** - is the main domain of your store. You can find it your store's domain by visiting the admin panel on a Home tab. There you can find the following message - Your current domain is xxx.myshopify.com

Picture

**API KEY** is used to receive your store's data like items and collections. To receive the key, you have to visit the admin panel and proceed to Apps - Manage Private Apps. Create a new application if you don't have one by copying Storefront API and adding it to your library's configuration

Picture

**ADMIN API KEY** is a key for Admin API. The library uses the key to receive a list of countries eligible to shipping.

**ADMIN PASSWORD** is a password for Admin API.  

# Requirements
* Android 4.4 (API 19) - a minimum supported version
* Android Studio for application build
* Gradle to install all the dependencies   

# License
ShopApp is licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
