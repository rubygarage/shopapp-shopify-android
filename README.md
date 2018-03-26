[![Build Status](https://travis-ci.org/rubygarage/shopapp-shopify-android.svg?branch=master)](https://travis-ci.org/rubygarage/shopapp-shopify-android)
[![codecov](https://codecov.io/gh/rubygarage/shopapp-shopify-android/branch/master/graph/badge.svg)](https://codecov.io/gh/rubygarage/shopapp-shopify-android)

# Shopify Provider for ShopApp Android

Shopify provider for [ShopApp for Android] (https://github.com/rubygarage/shopapp-android). ShopApp is an application that turns a Shopify-based store into a mobile app. 
ShopApp syncs with Shopify store and transfers a product catalog and user data to a mobile app. 
The app provides features like customizable push notifications, promo codes, and convenient payments with popular digital wallets like Android Pay.

![ ](https://github.com/rubygarage/shopapp-shopify-android/blob/master/assets/shopapp-main-screen.gif?raw=true)

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

## License
The ShopApp Shopify for iOS provider is licensed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0)
***
<a href="https://rubygarage.org/"><img src="https://github.com/rubygarage/shopapp-shopify-ios/blob/master/assets/rubygarage.png?raw=true" alt="RubyGarage Logo" width="415" height="128"></a>

RubyGarage is a leading software development and consulting company in Eastern Europe. Our main expertise includes Ruby and Ruby on Rails, but we successfuly employ other technologies to deliver the best results to our clients. [Check out our portoflio](https://rubygarage.org/portfolio) for even more exciting works!
