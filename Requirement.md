### **Phase 1: Core Functionality**
*(Based on your typed requirements)*

#### **1.1 Core Tenant & Unit Management**
* Admin can manage tenant records and lease durations.
* Assign tenant to a unit.
* Record check-in and check-out dates.
* Prevent conflicts in unit occupancy.

#### **1.2 Dashboard & Availability**
* Must have a dashboard to easily display the availability of all 24 rooms.
* (Specific requirement) Must clearly display 2 floors with 12 rooms each.

#### **1.3 Core Billing & Documents**
* Include rent amount and billing cycle (e.g., monthly, yearly).
* Generate and print/download receipts for rent payments (including electricity and water).
* Generate and print/download the rental contract.

#### **1.4 Core Maintenance**
* Track maintenance tasks and supply usage (e.g., light bulb replacement, air-con servicing, plumbing).
* Maintain a per-unit maintenance log.
* Scheduled reminders for recurring maintenance.

---

### **Phase 2: Advanced Features & Reporting**
*(Based on the requirements from the image)*

#### **2.1 Advanced Rates & Debt Management**
* Each room can have a different rate (at different times).
* When the room rate changes, the historical data must still show the previous rate in the old month.
* Unpaid or partial paid bills must accumulate to the next bill with a configurable interest rate.
* Add an optional field per room in case there's something extra to be charged.

#### **2.2 Advanced Invoicing & Payment Records**
* Add bank transfer details / QR payment to the invoice.
* Bulk print receipts.
* Keep payment and debt records.
* (Optional) Allow users to upload proof of payment (e.g., Bank transfer receipt or slip).

#### **2.3 Reporting & Analytics**
* Summary reports by unit, tenant, or month.
* Accept CSV as input for water/electricity usage.
* Graph showing water/electricity usage per room/floor per month/year.

#### **2.4 Stock & Document Management**
* Supply stock tracking & low inventory alerts.
* Store and retrieve scanned copies of contracts, maintenance reports, and receipts.

#### **2.5 System Configuration**
* The room count and floor count must be configurable (as opposed to the fixed 24 rooms/2 floors in Phase 1).

---

### **Bonus Features**
*(Not required, but will earn additional points)*

* **Predictive Maintenance:** Predict the next maintenance schedule (e.g., with an offline model or algorithm without AI).
* **Smart Search:** Smart search for renter names (with false tolerant), e.g., searching "krant" or "kart" must find "kant".
* **OCR (Optical Character Recognition):** Local OCR to auto-fill tenant details from uploaded contract images.
* **RBAC (Role-Based Access Control):** Different admin roles (e.g., Manager, Maintenance Staff, Accountant) with different permissions.