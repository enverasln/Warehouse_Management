# API Contract Document - Warehouse Management System

## Table of Contents
- [Overview](#overview)
- [Base URL](#base-url)
- [Authentication](#authentication)
- [Common Response Models](#common-response-models)
- [Error Handling](#error-handling)
- [API Endpoints](#api-endpoints)
  - [Authentication Service](#authentication-service)
  - [Warehouse Service](#warehouse-service)
  - [Order Service](#order-service)
  - [Stock Transaction Service](#stock-transaction-service)
  - [Barcode Definition Service](#barcode-definition-service)

---

## Overview

This document describes the API contract for the Warehouse Management System. The API is built using RESTful principles and uses JSON for request and response payloads.

### Technology Stack
- **Backend Framework**: ASP.NET Core / Spring Boot
- **API Style**: RESTful
- **Data Format**: JSON
- **Authentication**: JWT (JSON Web Token)

---

## Base URL

```
Production: https://api.warehouse-management.com
Development: https://dev-api.warehouse-management.com
```

---

## Authentication

### Authentication Method
The API uses JWT (JSON Web Token) for authentication. After a successful login, the server returns an access token that must be included in subsequent requests.

### Using the Access Token
Include the token in the Authorization header:
```
Authorization: Bearer {access_token}
```

### Token Expiration
Tokens have an expiration date included in the login response. Clients should monitor the expiration and refresh tokens as needed.

---

## Common Response Models

### PagedResponseModel<T>
Used for endpoints that return paginated data.

```json
{
  "items": [T],
  "index": 0,
  "size": 20,
  "count": 150,
  "pages": 8,
  "hasPrevious": false,
  "hasNext": true
}
```

**Fields:**
- `items`: Array of items of type T
- `index`: Current page index (0-based)
- `size`: Number of items per page
- `count`: Total number of items
- `pages`: Total number of pages
- `hasPrevious`: Boolean indicating if there's a previous page
- `hasNext`: Boolean indicating if there's a next page

### DataResponseModel<T>
Simple wrapper for single data responses.

```json
{
  "data": T
}
```

**Fields:**
- `data`: The response data of type T

---

## Error Handling

### Error Response Structure

```json
{
  "type": "string",
  "title": "string",
  "status": 400,
  "detail": "Business error message",
  "errors": {
    "fieldName": ["Validation error message 1", "Validation error message 2"]
  }
}
```

**Fields:**
- `type`: Error type identifier (optional)
- `title`: Human-readable error title (optional)
- `status`: HTTP status code (optional)
- `detail`: Business error message (optional)
- `errors`: Map of field names to validation error messages (optional)

### Common HTTP Status Codes
- `200 OK`: Successful request
- `201 Created`: Resource successfully created
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## API Endpoints

## Authentication Service

### 1. Login

**Endpoint:** `POST /authserver/auth/login`

**Description:** Authenticates a user and returns an access token.

**Request Body:**
```json
{
  "usernameOrEmail": "string",
  "password": "string"
}
```

**Request Fields:**
- `usernameOrEmail` (string, required): Username or email address
- `password` (string, required): User password

**Success Response:** `200 OK`
```json
{
  "accessToken": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expirationDate": "2026-02-08T13:58:05.077Z"
  }
}
```

**Response Fields:**
- `accessToken.token` (string): JWT access token
- `accessToken.expirationDate` (date): Token expiration date/time

**Error Responses:**
- `400 Bad Request`: Invalid credentials
- `401 Unauthorized`: Authentication failed

---

## Warehouse Service

### 1. Get All Warehouses

**Endpoint:** `GET /depo-service/warehouses/get-all`

**Description:** Retrieves a paginated list of all warehouses.

**Query Parameters:**
- `PageRequest.PageIndex` (integer, required): Page index (0-based)
- `PageRequest.PageSize` (integer, required): Number of items per page

**Example Request:**
```
GET /depo-service/warehouses/get-all?PageRequest.PageIndex=0&PageRequest.PageSize=20
```

**Success Response:** `200 OK`
```json
{
  "items": [
    {
      "id": "string",
      "warehouseNumber": 1,
      "warehouseName": "Main Warehouse",
      "location": "Istanbul",
      "isActive": true
    }
  ],
  "index": 0,
  "size": 20,
  "count": 150,
  "pages": 8,
  "hasPrevious": false,
  "hasNext": true
}
```

### 2. Get Warehouse by Number

**Endpoint:** `GET /depo-service/warehouses/{warehouseNumber}`

**Description:** Retrieves a specific warehouse by its warehouse number.

**Path Parameters:**
- `warehouseNumber` (integer, required): Unique warehouse number

**Example Request:**
```
GET /depo-service/warehouses/1
```

**Success Response:** `200 OK`
```json
{
  "id": "string",
  "warehouseNumber": 1,
  "warehouseName": "Main Warehouse",
  "location": "Istanbul",
  "isActive": true
}
```

**Error Responses:**
- `404 Not Found`: Warehouse not found

---

## Order Service

### 1. Get Planned Goods Acceptance Documents

**Endpoint:** `GET /depo-service/siparisler/planli-mal-kabul-evraklari`

**Description:** Retrieves a paginated list of planned goods acceptance documents based on search criteria.

**Query Parameters:**
- `PageRequest.PageIndex` (integer, required): Page index (0-based)
- `PageRequest.PageSize` (integer, required): Number of items per page
- `DepoNo` (integer, required): Warehouse number
- `CariUnvan` (string, required): Company name (can be empty string for all)
- `SiparisTarihi` (string, required): Order date in format (can be empty string for all)

**Example Request:**
```
GET /depo-service/siparisler/planli-mal-kabul-evraklari?PageRequest.PageIndex=0&PageRequest.PageSize=20&DepoNo=1&CariUnvan=&SiparisTarihi=
```

**Success Response:** `200 OK`
```json
{
  "items": [
    {
      "evrakSeri": "A",
      "evrakSira": 12345,
      "siparisTarihi": "2026-02-08T00:00:00Z",
      "cariUnvan": "ABC Company",
      "toplamMiktar": 100.0,
      "depoNo": 1
    }
  ],
  "index": 0,
  "size": 20,
  "count": 50,
  "pages": 3,
  "hasPrevious": false,
  "hasNext": true
}
```

**Response Fields:**
- `evrakSeri` (string): Document series
- `evrakSira` (integer): Document number
- `siparisTarihi` (date): Order date
- `cariUnvan` (string): Company name
- `toplamMiktar` (number): Total quantity
- `depoNo` (integer): Warehouse number

### 2. Get Planned Goods Acceptance Products

**Endpoint:** `GET /depo-service/siparisler/planli-mal-kabul-evraklari/urunler`

**Description:** Retrieves products for a specific planned goods acceptance document.

**Query Parameters:**
- `EvrakSeri` (string, required): Document series
- `EvrakSira` (integer, required): Document number
- `DepoNo` (integer, required): Warehouse number

**Example Request:**
```
GET /depo-service/siparisler/planli-mal-kabul-evraklari/urunler?EvrakSeri=A&EvrakSira=12345&DepoNo=1
```

**Success Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "tarih": "2026-02-08T00:00:00Z",
    "evrakSeri": "A",
    "evrakSira": 12345,
    "evrakSatirNumarasi": 1,
    "stokId": "uuid",
    "stokKodu": "STK001",
    "stokAdi": "Product Name",
    "barkod": "1234567890123",
    "firmaId": "uuid",
    "firmaKodu": "CMP001",
    "firmaAdi": "ABC Company",
    "odemePlanNo": 1,
    "depoId": "uuid",
    "depoNo": 1,
    "depoAdi": "Main Warehouse",
    "miktar": 100.0,
    "dovizCinsi": 1,
    "iskonto1": 0.0,
    "iskonto2": 0.0,
    "iskonto3": 0.0,
    "iskonto4": 0.0,
    "iskonto5": 0.0,
    "birimFiyat": 10.50,
    "tutar": 1050.0,
    "vergiPntr": 1,
    "cariSorumlulukMerkezi": "CM01",
    "stokSorumlulukMerkezi": "SM01",
    "kalanMiktar": 50.0,
    "renkliBedenliMi": false
  }
]
```

**Response Fields:**
- `id` (string): Unique product ID
- `tarih` (date): Order date
- `evrakSeri` (string): Document series
- `evrakSira` (integer): Document number
- `evrakSatirNumarasi` (integer): Document line number
- `stokId` (string): Stock ID
- `stokKodu` (string): Stock code
- `stokAdi` (string): Stock name
- `barkod` (string): Barcode
- `firmaId` (string): Company ID
- `firmaKodu` (string): Company code
- `firmaAdi` (string): Company name
- `odemePlanNo` (integer): Payment plan number
- `depoId` (string): Warehouse ID
- `depoNo` (integer): Warehouse number
- `depoAdi` (string): Warehouse name
- `miktar` (number): Quantity
- `dovizCinsi` (byte): Currency type
- `iskonto1-5` (number): Discount amounts 1-5
- `birimFiyat` (number): Unit price
- `tutar` (number): Total price
- `vergiPntr` (byte): VAT pointer
- `cariSorumlulukMerkezi` (string): Current responsibility center
- `stokSorumlulukMerkezi` (string): Stock responsibility center
- `kalanMiktar` (number): Remaining quantity
- `renkliBedenliMi` (boolean): Is colored and sized

### 3. Get Next Order Document Series and Number

**Endpoint:** `GET /depo-service/siparisler/planli-mal-kabul-evraklari/siradaki-evrak-seri-sira`

**Description:** Retrieves the next available document series and number for an order.

**Query Parameters:**
- `SipTip` (byte, required): Order type
- `SipCins` (byte, required): Order kind
- `EvraknoSeri` (string, required): Document series

**Example Request:**
```
GET /depo-service/siparisler/planli-mal-kabul-evraklari/siradaki-evrak-seri-sira?SipTip=1&SipCins=1&EvraknoSeri=A
```

**Success Response:** `200 OK`
```json
{
  "evrakSeri": "A",
  "evrakSira": 12346
}
```

**Response Fields:**
- `evrakSeri` (string): Document series
- `evrakSira` (integer): Next available document number

### 4. Add Order

**Endpoint:** `POST /depo-service/siparisler/normal-siparis-ekle`

**Description:** Creates a new order in the system.

**Request Body:**
```json
{
  "recordId": "uuid",
  "evrakTarihi": "2026-02-08",
  "seri": "A",
  "sira": 12346,
  "satir": 1,
  "stokKod": "STK001",
  "cariKod": "CMP001",
  "miktar": 100.0,
  "girisDepo": 1,
  "cikisDepo": 0,
  "plasiyer": "SLS001",
  "srMerkez": "CM01",
  "kullanici": 1,
  "yekun": 1050.0,
  "isk1Tutar": 0.0,
  "isk2Tutar": 0.0,
  "isk3Tutar": 0.0,
  "isk4Tutar": 0.0,
  "isk5Tutar": 0.0,
  "kdv": 1,
  "fiyat": 10.50,
  "belgeNo": "DOC001",
  "firmaNo": 1,
  "subeNo": 1,
  "barkod": "1234567890123",
  "renkliBedenliMi": false
}
```

**Request Fields:**
- `recordId` (string): Unique record ID
- `evrakTarihi` (string): Order date (format: YYYY-MM-DD)
- `seri` (string): Document series
- `sira` (integer): Document number
- `satir` (integer): Line number
- `stokKod` (string): Stock code
- `cariKod` (string): Current/company code
- `miktar` (number): Quantity
- `girisDepo` (integer): Input warehouse number
- `cikisDepo` (integer): Output warehouse number
- `plasiyer` (string): Salesman code
- `srMerkez` (string): Responsibility center
- `kullanici` (integer): User code
- `yekun` (number): Total price
- `isk1Tutar-isk5Tutar` (number): Discount amounts 1-5
- `kdv` (byte): VAT pointer
- `fiyat` (number): Unit price
- `belgeNo` (string): Paper/document number
- `firmaNo` (integer): Company number
- `subeNo` (integer): Store/branch number
- `barkod` (string): Barcode
- `renkliBedenliMi` (boolean): Is colored and sized

**Success Response:** `200 OK`

**Error Responses:**
- `400 Bad Request`: Invalid request data
- `409 Conflict`: Document already exists

### 5. Check Document Availability

**Endpoint:** `GET /depo-service/siparisler/check-document-is-usable`

**Description:** Checks if a document series and number combination is available for use.

**Query Parameters:**
- `TransactionType` (byte, required): Transaction type
- `TransactionKind` (byte, required): Transaction kind
- `DocumentSeries` (string, required): Document series
- `DocumentNumber` (integer, required): Document number

**Example Request:**
```
GET /depo-service/siparisler/check-document-is-usable?TransactionType=1&TransactionKind=1&DocumentSeries=A&DocumentNumber=12346
```

**Success Response:** `200 OK`
```json
{
  "isAvailable": true,
  "message": "Document is available"
}
```

**Response Fields:**
- `isAvailable` (boolean): Whether the document is available
- `message` (string): Status message

---

## Stock Transaction Service

### 1. Check Document Usability

**Endpoint:** `GET /depo-service/stok-hareketleri/check-document-usable`

**Description:** Checks if a stock transaction document is usable.

**Query Parameters:**
- `evrakNoSeri` (string, required): Document series
- `evrakNoSira` (integer, required): Document number
- `cariKod` (string, optional): Company code
- `belgeNo` (string, optional): Paper number
- `StokHareketTipi` (byte, required): Stock transaction type
- `StokHareketCinsi` (byte, required): Stock transaction kind
- `StokHareketEvrakTipi` (byte, required): Document type
- `StokHareketIslemTipi` (byte, required): Is normal or return

**Example Request:**
```
GET /depo-service/stok-hareketleri/check-document-usable?evrakNoSeri=A&evrakNoSira=12346&cariKod=CMP001&belgeNo=DOC001&StokHareketTipi=1&StokHareketCinsi=1&StokHareketEvrakTipi=1&StokHareketIslemTipi=1
```

**Success Response:** `200 OK`
```json
{
  "isUsable": true,
  "message": "Document is usable"
}
```

**Response Fields:**
- `isUsable` (boolean): Whether the document is usable
- `message` (string): Status message

### 2. Add Stock Transaction

**Endpoint:** `POST /depo-service/stok-hareketleri/terminal-shar-ekle`

**Description:** Creates a new stock transaction (terminal stock movement).

**Request Body:**
```json
{
  "tip": 1,
  "cins": 1,
  "islemTipi": 1,
  "evrakTip": 1,
  "evrakTarihi": "2026-02-08",
  "seri": "A",
  "sira": 12346,
  "satir": 1,
  "stokKod": "STK001",
  "cariKod": "CMP001",
  "miktar": 100.0,
  "girisDepoNo": 1,
  "cikisDepoNo": 0,
  "odeme": 1,
  "plasiyer": "SLS001",
  "srmerkez": "CM01",
  "kullanici": 1,
  "yekun": 1050.0,
  "isk1Tutar": 0.0,
  "isk2Tutar": 0.0,
  "isk3Tutar": 0.0,
  "isk4Tutar": 0.0,
  "isk5Tutar": 0.0,
  "kdv": 1,
  "refRec": "uuid",
  "fiyat": 10.50,
  "belgeNo": "DOC001",
  "firmaNo": 1,
  "subeNo": 1,
  "barkod": "1234567890123",
  "sthNakliyeDurumu": 0,
  "recordId": "uuid",
  "renkliBedenliMi": false
}
```

**Request Fields:**
- `tip` (byte): Stock transaction type
- `cins` (byte): Stock transaction kind
- `islemTipi` (byte): Is normal or return (1=Normal, 2=Return)
- `evrakTip` (byte): Document type
- `evrakTarihi` (string): Document date (format: YYYY-MM-DD)
- `seri` (string): Document series
- `sira` (integer): Document number
- `satir` (long): Line number
- `stokKod` (string): Stock code
- `cariKod` (string): Company code
- `miktar` (number): Quantity
- `girisDepoNo` (integer): Input warehouse number
- `cikisDepoNo` (integer): Output warehouse number
- `odeme` (integer): Payment plan number
- `plasiyer` (string): Salesman code
- `srmerkez` (string): Responsibility center
- `kullanici` (integer): User code
- `yekun` (number): Total price
- `isk1Tutar-isk5Tutar` (number): Discount amounts 1-5
- `kdv` (byte): Tax pointer
- `refRec` (string, nullable): Reference order ID
- `fiyat` (number): Unit price
- `belgeNo` (string): Paper/document number
- `firmaNo` (integer): Company number
- `subeNo` (integer): Store/branch number
- `barkod` (string): Barcode
- `sthNakliyeDurumu` (byte): Transportation status
- `recordId` (string): Unique record ID
- `renkliBedenliMi` (boolean): Is colored and sized

**Success Response:** `200 OK`

**Error Responses:**
- `400 Bad Request`: Invalid request data
- `409 Conflict`: Document already exists

### 3. Get Next Stock Transaction Document

**Endpoint:** `GET /depo-service/stok-hareketleri/get-next-document`

**Description:** Retrieves the next available stock transaction document number.

**Query Parameters:**
- `stockTransactionType` (byte, required): Stock transaction type
- `stockTransactionKind` (byte, required): Stock transaction kind
- `IsStockTransactionNormalOrReturn` (byte, required): Is normal or return
- `stockTransactionDocumentType` (byte, required): Stock transaction document type
- `documentSeries` (string, required): Document series

**Example Request:**
```
GET /depo-service/stok-hareketleri/get-next-document?stockTransactionType=1&stockTransactionKind=1&IsStockTransactionNormalOrReturn=1&stockTransactionDocumentType=1&documentSeries=A
```

**Success Response:** `200 OK`
```json
{
  "data": {
    "documentSeries": "A",
    "documentNumber": 12347
  }
}
```

**Response Fields:**
- `data.documentSeries` (string): Document series
- `data.documentNumber` (integer): Next available document number

---

## Barcode Definition Service

### 1. Get Barcode Definition by Barcode

**Endpoint:** `GET /depo-service/barcode-definitions/get-by-barcode`

**Description:** Retrieves product/stock information by scanning a barcode.

**Query Parameters:**
- `barcode` (string, required): Barcode value
- `warehouseNumber` (integer, required): Warehouse number

**Example Request:**
```
GET /depo-service/barcode-definitions/get-by-barcode?barcode=1234567890123&warehouseNumber=1
```

**Success Response:** `200 OK`
```json
{
  "id": "uuid",
  "barcode": "1234567890123",
  "stockCode": "STK001",
  "stockName": "Product Name",
  "warehouseNumber": 1,
  "quantity": 150.0,
  "unitPrice": 10.50,
  "isActive": true
}
```

**Response Fields:**
- `id` (string): Unique ID
- `barcode` (string): Barcode value
- `stockCode` (string): Stock code
- `stockName` (string): Stock/product name
- `warehouseNumber` (integer): Warehouse number
- `quantity` (number): Available quantity
- `unitPrice` (number): Unit price
- `isActive` (boolean): Active status

**Error Responses:**
- `404 Not Found`: Barcode not found

---

## Enumerations

### Stock Transaction Types
- `1`: Goods Receipt
- `2`: Goods Issue
- `3`: Transfer

### Stock Transaction Kinds
- `1`: Purchase
- `2`: Sales
- `3`: Production
- `4`: Warehouse Transfer

### Stock Transaction Document Types
- `1`: Invoice
- `2`: Waybill
- `3`: Warehouse Transfer Document
- `4`: Production Document

### Order Transaction Types
- `1`: Purchase Order
- `2`: Sales Order

### Order Transaction Kinds
- `1`: Normal Order
- `2`: Return Order
- `3`: Consignment Order

### Transportation Status
- `0`: Not Shipped
- `1`: In Transit
- `2`: Delivered

---

## Best Practices

### Pagination
- Always use pagination for list endpoints to improve performance
- Default page size should be reasonable (e.g., 20-50 items)
- Maximum page size should be enforced server-side

### Date Formats
- Use ISO 8601 format for dates: `YYYY-MM-DDTHH:mm:ss.sssZ`
- For date-only fields: `YYYY-MM-DD`

### Error Handling
- Always check HTTP status codes
- Parse error responses to provide user-friendly messages
- Handle network errors gracefully

### Security
- Never expose sensitive data in URLs (use request body for sensitive data)
- Always validate and sanitize input data
- Use HTTPS for all API communications
- Store JWT tokens securely (never in localStorage for web apps)

### Performance
- Cache responses when appropriate
- Use pagination for large datasets
- Implement request throttling/rate limiting awareness

---

## Versioning

The API follows semantic versioning. Breaking changes will result in a new API version.

**Current Version:** v1

Future versions will be accessible via URL path:
```
/api/v2/endpoint
```

---

## Support

For API support and questions:
- Email: api-support@warehouse-management.com
- Documentation: https://docs.warehouse-management.com

---

**Document Version:** 1.0  
**Last Updated:** 2026-02-08  
**Maintained by:** Warehouse Management Development Team
