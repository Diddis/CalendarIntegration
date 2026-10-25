# TimeEdit to Canvas Schedule Integrator

A Service-Oriented Architecture (SOA) proof-of-concept designed to automate the synchronization of university scheduling data.

---

## Purpose

A major challenge facing higher education is the growing burden of administrative tasks. While vital for course organization, these demands heavily consume the time instructors could spend on core teaching. 

A particularly time-consuming and error-prone bottleneck is the manual transfer of finalized schedule data from the scheduling system (TimeEdit) to the online course platform (Canvas). Because these systems lack native integration, course coordinators must manually re-enter scheduled events, leading to redundant work and the risk of displaying inconsistent information to students.

### The Solution
This project applies **Enterprise Architecture (EA)** principles to analyze the existing scheduling process, identify key shortcomings, and develop an automated system integration. Using a **Service-Oriented Architecture (SOA)** approach, we developed a proof-of-concept prototype that securely retrieves schedule information from the TimeEdit API and registers it directly into the Canvas calendar via its open API. 

*Note: The prototype is limited to demonstrating technical feasibility and does not constitute a full-scale solution ready for production implementation.*

---

## 🎓 Context & My Role

This project was developed collaboratively as part of the **"Enterprise Architecture using SOA"** course at **Luleå University of Technology**. I spearheaded the **design and implementation of the backend solution**, transforming the architectural theory into a functional programmatic prototype.

---

## Method & Architecture

### Enterprise Architecture Framework
The work began with an analysis of the current state of the scheduling process, including identifying shortcomings, bottlenecks, and areas for improvement. Subsequently, TOGAF—a framework and methodology for analyzing and planning enterprise architecture—was used in conjunction with ArchiMate to define and develop a desired future state. 

### Technical Stack
* **Backend:** Built using **Java** and the **Spring Boot** framework within a **Maven**-based build environment.
* **Frontend:** A responsive user interface developed using vanilla **HTML**, **JavaScript**, and **CSS**.
* **Data Management:** Designed for secure, lightweight execution. The application uses **in-memory processing** to read data from TimeEdit and transfer it directly to Canvas; no external databases or files are used for persistent data storage.
