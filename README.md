


🌾 KisanMitra AI
Know Your Crop. Know Your Market. Know Your Profit.
KisanMitra AI is an AI-powered agricultural decision-support and marketplace application designed to help farmers make better crop-selling decisions.

Instead of simply showing the current mandi price, KisanMitra combines crop condition, spoilage risk, farmer-specific costs, market trends, price forecasts, and buyer competition to help farmers decide:

When should I sell, whom should I sell to, and will I actually make a profit?

🚜 Problem
Small and marginal farmers often make crop-selling decisions with incomplete information.

A farmer may know today's market price, but may not know:

What his actual break-even price is

Whether today's price gives him a profit or loss

Whether the price may improve in the next few days

Whether his crop can safely wait

Which buyer can offer the best price

Whether storage or transportation is available

For perishable crops, this information gap can lead to distress selling, low margins, and post-harvest losses.

💡 Our Solution
KisanMitra AI brings these decisions together in one platform.

Core Decision Flow
Farmer Expenses
      +
Crop & Harvest Information
      +
Market Prices
      +
Price Forecast
      +
Weather / Spoilage Risk
      +
Buyer Bids
      ↓
AI-Powered Selling Recommendation
      ↓
SELL NOW / WAIT
      ↓
Competitive Auction
      ↓
Final Profit & Loss
Example
A farmer has tomatoes and receives the following market information:

Today's market price → ₹12/kg

Expected price in 2 days → ~₹15/kg

Farmer break-even → ₹16/kg

Remaining safe shelf life → 3 days

KisanMitra evaluates the complete situation instead of displaying only the market price. It helps the farmer understand whether waiting or selling now may be financially better.

We don't just show the market price. We explain what that price means for the farmer.

🎯 Core Product Idea
KisanMitra AI answers one critical question:

“Should I sell my crop today, or can I safely wait for a better price?”

The system connects four decision layers:

Forecast decides WHEN
          +
Spoilage decides WHETHER the farmer can wait
          +
Auction discovers HOW MUCH buyers will pay
          +
P&L decides WHETHER it is profitable
✨ Key Features
🌱 1. Crop Intelligence
Monitor:

Crop health

Spoilage risk

Remaining shelf life

Crop condition

Harvest information

📈 2. Market Intelligence
Farmers can view:

Yesterday's price

Today's price

Tomorrow's expected price

Price movement through line charts

Market trends

Mandi information

The Market screen uses a simple trading-style line chart so farmers can understand whether prices are moving up or down.

Tomorrow's value is clearly presented as a forecast, not a guaranteed price.

Example
Yesterday      ₹13/kg
Today          ₹15.20/kg
Tomorrow       ~₹17/kg
💰 3. Farmer-Specific P&L
KisanMitra calculates financial information based on the farmer's actual expenses.

The system can track:

Seeds

Fertilizer

Labour

Irrigation

Transportation

Other crop expenses

It calculates:

Total Cost
Break-even Price
Selling Price
Revenue
Profit / Loss
Price alone is not enough. The farmer needs to know what that price means for his own crop and costs.

🤖 4. Multilingual AI Assistant
Farmers can interact with KisanMitra using natural language.

The application is designed to support:

English

Hindi

Marathi

Gujarati

Urdu

Bengali

Tamil

Telugu

Voice-to-Action
The goal is not just speech-to-text.

A farmer can speak naturally, for example:

“Aaj khaad pe 800 rupaye lage.”

KisanMitra can understand:

Action   → Add Expense
Category → Fertilizer
Amount   → ₹800
Crop     → Current Crop
Date     → Today
🔨 5. Reverse Auction
Instead of depending on a single buyer, farmers can open an auction.

Example:

Buyer A → ₹15/kg
Buyer B → ₹17/kg
Buyer C → ₹19/kg
Buyer D → ₹21/kg
Multiple buyers compete for the same produce.

Core idea
We don't just help farmers find buyers. We create competition between buyers.

🧠 6. Sell Now vs Wait Decision
The decision engine considers:

Current Price
+
Price Forecast
+
Break-even Price
+
Spoilage Risk
+
Remaining Shelf Life
+
Buyer Bids
Then it provides a simple recommendation:

SELL TODAY
or

WAIT FOR A BETTER OPPORTUNITY
The system should not present forecasts as guarantees.

🧊 7. Storage & Supply Chain
KisanMitra is designed to connect the agricultural supply chain:

Farmer
   ↓
Buyer
   ↓
Transport Provider
   ↓
Warehouse / Cold Storage
   ↓
Consumer
The platform can connect farmers with:

Transportation

Cold storage

Warehousing

Buyers

Consumers

👥 User Roles
KisanMitra is designed for multiple participants in the agricultural supply chain.

Role	Purpose
👨‍🌾 Farmer	Manage crops, expenses, spoilage, auctions and P&L
🏪 Buyer	Discover crops and participate in auctions
🛒 Consumer	Purchase fresh produce
🚚 Transport Provider	Manage produce deliveries
🧊 Warehouse / Cold Storage Owner	Provide storage capacity
👨‍💼 Admin	Manage and monitor the platform
📱 Application Flow
App Launch
    ↓
3-Step Onboarding
    ↓
Language Selection
    ↓
Role Selection
    ↓
Authentication
    ↓
Profile Setup
    ↓
Role-specific Dashboard
3-Step Onboarding
1. Know your crop.
2. Know your market.
3. Know your profit.
Farmer Flow
Farmer
  ↓
Add Crop
  ↓
Track Expenses
  ↓
Calculate Break-even
  ↓
Monitor Spoilage
  ↓
Check Market Price
  ↓
View 2-Day Forecast
  ↓
SELL NOW / WAIT
  ↓
Reverse Auction
  ↓
Buyer Competition
  ↓
Sale
  ↓
Final P&L
🔐 Authentication Flow
The application architecture supports role-aware authentication.

Phone Authentication
Mobile Number
      ↓
OTP
      ↓
Authentication
Google Authentication
Google Sign-In
      ↓
Firebase Authentication
The selected role and language are part of the onboarding/session state and are intended to persist with the user profile.

During current UI development, a mock authentication layer may be used so the rest of the product can be built and tested quickly. Real Firebase Authentication is the target integration.

🌐 Localization
KisanMitra is designed for multilingual adoption.

Supported Languages
English
Hindi
Marathi
Gujarati
Urdu
Bengali
Tamil
Telugu
The selected language should control the application UI.

Example:

Language = Marathi
        ↓
Role Selection in Marathi
        ↓
Authentication in Marathi
        ↓
Profile in Marathi
        ↓
Farmer Dashboard in Marathi
        ↓
Market in Marathi
Language changes should affect translations, not the application's light-theme color system.

🏠 Farmer Dashboard
The Farmer Dashboard follows a compact, card-based mobile design.

Dashboard Sections
Farmer greeting + location

Crop tabs

Crop health summary

Spoilage risk

Current mandi price

Selling opportunity

Price forecast

Profit outlook

AI assistant

Bottom navigation

Dashboard Bottom Navigation
🏠 Home
🛒 Market
⭐ AI
💰 Finances
👤 Profile
The notification bell is intended to appear on authenticated role dashboards, not on the landing/onboarding screens.

📊 Market Price Module
The Market module opens from the dashboard's Market tab.

Crop Selection
🍅 Tomato
🥔 Potato
🧅 Onion
Time Selection
Yesterday
Today
Tomorrow
Example Market View
Nashik Mandi

Current Price:
₹15.20/kg

vs Yesterday:
↑ 16.9%

Yesterday:
₹13/kg

Today:
₹15.20/kg

Tomorrow Forecast:
~₹17/kg
Chart
The market visualization uses a simple line chart.

Actual Market Data
────────────── solid line

AI Forecast
- - - - - - - dotted/dashed line
The design is intentionally simpler than a stock-market trading terminal so farmers can understand it quickly.

🧮 Recommendation Engine
The Market and dashboard decision engine is intended to combine:

Current Price
       +
Forecast Price
       +
Break-even Price
       +
Remaining Shelf Life
       +
Best Buyer Bid
       +
Forecast Confidence
       ↓
Selling Recommendation
Example
Current Price      ₹15/kg
Break-even         ₹16/kg
2-Day Forecast     ~₹17/kg
Shelf Life         3 days
Potential output:

WAIT FOR TOMORROW

with a simple explanation such as:

Tomorrow's expected price is higher than today's and the crop can safely wait.

If spoilage risk is high or the crop cannot safely wait:

SELL TODAY

Forecasts remain estimates and are not guarantees.

🎨 UI / UX
KisanMitra follows a modern, minimal, farmer-friendly design system.

Design Characteristics
Clean mobile-first interface

Soft agricultural color palette

Rounded cards

Pill-shaped controls

Simple icons

High readability

Responsive Android layouts

Multilingual interface

Dashboard-oriented information architecture

Light-first visual theme

Main Screens
3-Step Onboarding

Language Selection

Role Selection

Authentication

OTP Verification

Profile Setup

Farmer Dashboard

Market Price

AI Assistant

Finances / P&L

Crop Management

Auctions

Profile

🛠️ Technology Stack
Android / Frontend
Kotlin

Jetpack Compose

Material 3

Android SDK

Jetpack Credential Manager

Backend / Cloud
Firebase

Firebase Authentication

Cloud Firestore

Firebase Storage

Firebase Cloud Messaging

AI
Google Gemini

Multilingual conversational AI

Voice understanding

Text understanding

Image/OCR assistance

AI recommendations

Market Intelligence
AGMARKNET

e-NAM

Historical mandi-price data

Weather & Location
Weather data APIs

Google Maps Platform

Prediction / Decision Support
Price forecasting

Rule-based spoilage analysis

Farmer-specific P&L calculations

Buyer/market matching

Development & Design
Google AI Studio

AI-assisted UI generation and development

🏗️ High-Level Architecture
                    KISANMITRA AI
                         │
          ┌──────────────┴──────────────┐
          │                             │
       Android                       AI Layer
   Kotlin + Compose                  Gemini
          │                             │
          └──────────────┬──────────────┘
                         │
                    Firebase
                         │
        ┌────────────────┼────────────────┐
        │                │                │
 Authentication      Firestore        Storage
        │                │
        │          User / Crop /
        │          Expense / Auction
        │
        └────────────────┬────────────────┘
                         │
                  Decision Engine
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
   Market Data        Weather         Spoilage
   AGMARKNET/eNAM                       Risk
       │                 │                 │
       └─────────────────┼─────────────────┘
                         │
                  Price Forecast
                         │
                  Selling Decision
                         │
                  Reverse Auction
🔔 Notification System
Role-specific notifications can include:

Farmer
Crop spoilage alert

Price forecast update

New auction bid

Auction result

Storage reminder

Buyer
New crop auction

Outbid alert

Bid accepted

Consumer
Order update

Delivery update

Fresh produce availability

Transport Provider
Delivery request

Pickup reminder

Delivery completion

Storage Owner
New storage booking

Booking reminder

Admin
New user

Verification request

Platform alert

The notification bell is intended to appear only on authenticated role dashboards.

💼 Business Model
KisanMitra is designed around a sustainable marketplace model.

Primary Revenue
Small transaction/platform fee on successful marketplace transactions.

Farmer sells
     ↓
Buyer receives produce
     ↓
Successful transaction
     ↓
KisanMitra earns a platform fee
The goal is to align platform revenue with successful transactions.

Future Revenue Opportunities
Premium analytics for buyers and FPOs

Logistics partnerships

Storage partnerships

Marketplace services

Advanced agricultural insights

🌾 Expected Impact
KisanMitra aims to:

Reduce distress selling

Improve price discovery

Improve farmer profitability

Reduce avoidable post-harvest losses

Improve access to buyers

Connect farmers with transport and storage

Improve transparency across the agricultural supply chain

SDG Alignment
SDG 2 — Zero Hunger

SDG 8 — Decent Work and Economic Growth

SDG 12 — Responsible Consumption and Production

🔬 Research & References
The KisanMitra concept is informed by agricultural market, post-harvest, and food-loss research and by publicly available agricultural data sources.

Reference Sources
AGMARKNET — Agricultural market prices and arrivals

e-NAM — National Agriculture Market

ICAR — Indian Council of Agricultural Research

FAO — Food and Agriculture Organization

National Horticulture Board (NHB)

Government agricultural datasets

Weather and market data sources

Research Themes
Agricultural market-price intelligence

Post-harvest loss reduction

Crop shelf-life and storage practices

Farmer profitability and break-even analysis

Digital marketplaces and price discovery

AI-assisted agricultural decision support

🚧 Current Development Status
KisanMitra AI is currently under active development.

Current Development Areas
✅ Android UI / onboarding

✅ 3-step onboarding concept

✅ Language Selection

✅ Role Selection

✅ Farmer Authentication UI

✅ OTP Verification UI

✅ Farmer Profile UI

✅ Farmer Dashboard UI

✅ Market Price UI

✅ Price visualization concept

✅ P&L concept

✅ Auction concept

✅ AI Assistant concept

✅ Responsive UI iteration

🔄 Authentication integration/testing

🔄 Real market-data integration

🔄 Price forecasting engine

🔄 Spoilage prediction engine

🔄 Multilingual voice-to-action integration

🔄 Full marketplace workflows

Some values shown in the current prototype are mock/demo data and are intended to be replaced by production data sources and prediction models.

🗺️ Development Roadmap
Phase 1
✅ UI/UX
✅ 3-Step Onboarding
✅ Language Selection
✅ Role Management
✅ Authentication UI
✅ Farmer Dashboard
✅ Market UI

        ↓

Phase 2
🔄 Authentication integration
🔄 Farmer Profile
🔄 P&L Ledger
🔄 Market Data Integration

        ↓

Phase 3
🔄 Price Forecasting
🔄 Spoilage Prediction
🔄 Multilingual Voice Assistant
🔄 Reverse Auction

        ↓

Phase 4
🔄 Transport
🔄 Cold Storage
🔄 Consumer Marketplace
🔄 FPO / Collective Selling

        ↓

Phase 5
🚀 Production Deployment
🚀 Pan-India Expansion
🎥 Hackathon / Prototype Demo Flow
For demonstration, the core farmer journey is:

Ramesh / Farmer
       ↓
Select Crop
       ↓
Add Expenses
       ↓
Break-even Price
       ↓
Spoilage Status
       ↓
Today's Mandi Price
       ↓
2-Day Price Forecast
       ↓
SELL NOW / WAIT
       ↓
Reverse Auction
       ↓
Buyer Competition
       ↓
Final P&L
Key Demo Message
KisanMitra does not just tell a farmer the market price. It tells him what that price means for his crop, his costs, his risk, and his profit.

👨‍💻 Project
KisanMitra AI

Team
Team Ghost Variables

Built as an agricultural technology project focused on improving crop-selling decisions and connecting the agricultural supply chain.

⭐ Vision
A farmer should not have to guess when to sell.

KisanMitra AI aims to give farmers the information needed to understand their crop, market, risk, and profit before making a selling decision.

📄 License
This project is currently being developed as an educational and hackathon project.

License information will be added as the project moves toward public/open-source release.
