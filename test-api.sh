#!/bin/bash

# Order Processing System - Test Script
# This script demonstrates the API functionality

BASE_URL="http://localhost:8080"
CREDENTIALS="user:password"

echo "🚀 Order Processing System - API Test Script"
echo "============================================="
echo ""

# Function to make authenticated requests
make_request() {
    curl -s -u "$CREDENTIALS" "$@"
}

# Function to pretty print JSON
pretty_print() {
    python3 -m json.tool 2>/dev/null || cat
}

echo "1. Creating a new order..."
ORDER_RESPONSE=$(make_request -X POST "$BASE_URL/api/orders" \
    -H "Content-Type: application/json" \
    -d '{
        "customerName": "Test Customer",
        "productName": "Test Product",
        "quantity": 2,
        "price": 199.99,
        "customerEmail": "test@example.com",
        "customerPhone": "+1234567890"
    }')

echo "$ORDER_RESPONSE" | pretty_print
ORDER_ID=$(echo "$ORDER_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null)

if [ -z "$ORDER_ID" ]; then
    echo "❌ Failed to create order. Please check if the application is running."
    exit 1
fi

echo ""
echo "✅ Order created with ID: $ORDER_ID"
echo ""

echo "2. Retrieving the order..."
make_request -X GET "$BASE_URL/api/orders/$ORDER_ID" | pretty_print
echo ""

echo "3. Updating order status to COMPLETED..."
make_request -X PUT "$BASE_URL/api/orders/$ORDER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "COMPLETED"}' | pretty_print
echo ""

echo "4. Searching for orders..."
make_request -X GET "$BASE_URL/api/orders/search?customerName=Test&page=0&size=10" | pretty_print
echo ""

echo "5. Trying invalid status transition (should fail)..."
make_request -X PUT "$BASE_URL/api/orders/$ORDER_ID/status" \
    -H "Content-Type: application/json" \
    -d '{"status": "CANCELLED"}' | pretty_print
echo ""

echo "✅ Test script completed!"
echo ""
echo "📋 Summary:"
echo "- Created order with ID: $ORDER_ID"
echo "- Retrieved order details"
echo "- Updated status to COMPLETED"
echo "- Searched orders"
echo "- Tested invalid status transition"
echo ""
echo "🌐 Access H2 Console at: http://localhost:8080/h2-console"
echo "📖 API Documentation available in README.md"