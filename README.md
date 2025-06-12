# AI-Powered Resume Builder and Job Matcher

This is a full-stack web application designed to help job seekers optimize their resumes for specific job descriptions. By leveraging the power of Google's Gemini Pro API, this tool provides a detailed analysis, a job-fit score, and actionable feedback to improve a candidate's chances of landing an interview.

## ✨ Features

- **Secure User Authentication**: JWT-based authentication system for user registration and login.
- **Resume Upload**: Supports multiple file formats (`.pdf`, `.doc`, `.docx`) for resume uploads.
- **AI-Powered Analysis**: Users can submit their resume and a job description to receive:
  - A **Job Score**: A percentage indicating how well the resume matches the job requirements.
  - **Targeted Changes**: Specific suggestions on what to modify in the resume, categorized by section.
  - **Overall Improvements**: General feedback on the resume's structure, language, and content.
- **Analysis History**: Users can view a history of their past analyses.
- **Responsive Frontend**: A clean and modern user interface built with React and Tailwind CSS.
- **Automatic Data Cleanup**: A scheduled service automatically soft-deletes resumes older than one hour to manage data privacy and retention.

## 🛠️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3**: For building the robust RESTful API.
- **Spring Security**: For handling authentication and authorization with JWT.
- **Spring Data JPA**: For database interactions.
- **PostgreSQL (Neon DB)**: As the primary relational database.
- **Maven**: For dependency management.
- **Google Gemini Pro**: Via the `google-genai` SDK for AI-driven text analysis.

### Frontend
- **React 18**: For building the user interface.
- **React Router v6**: For client-side routing.
- **Tailwind CSS**: For utility-first styling.
- **React Context API**: For state management (`AuthContext`, `AnalysisContext`).
- **Axios**: For making HTTP requests to the backend API.
- **Vite**: As the frontend build tool.

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java JDK 17 or later
- Maven 3.8 or later
- Node.js 18 or later
- npm/yarn
- A PostgreSQL database instance (e.g., from [Neon](https://neon.tech/))
- A Google Gemini API Key (from [Google AI Studio](https://aistudio.google.com/))

### Backend Setup

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    ```

2.  **Navigate to the backend directory:**
    ```bash
    cd AI-Powered-Resume-Builder-and-Job-Matcher/backend/resumeTracker
    ```

3.  **Configure the application:**
    Open `src/main/resources/application.properties` and update the following properties with your credentials:

    ```properties
    # Database Configuration
    spring.datasource.url=jdbc:postgresql://<your-neon-db-host>/<db-name>
    spring.datasource.username=<your-username>
    spring.datasource.password=<your-password>

    # JPA Configuration
    spring.jpa.hibernate.ddl-auto=update

    # JWT Secret Key
    app.jwtSecret=your-super-secret-key-that-is-long-enough
    app.jwtExpirationMs=86400000

    # Google Gemini API Configuration
    gemini.api.key=AIzaSy...YOUR_API_KEY
    google.cloud.project.id=weighty-bounty-462404-b8
    google.cloud.location=asia-south1
    ```

4.  **Install dependencies and run the server:**
    ```bash
    mvn spring-boot:run
    ```
    The backend server will start on `http://localhost:8080`.

### Frontend Setup

1.  **Navigate to the frontend directory:**
    ```bash
    cd ../../frontend/project
    ```

2.  **Install dependencies:**
    ```bash
    npm install
    ```

3.  **Start the development server:**
    ```bash
    npm run dev
    ```
    The frontend application will be available at `http://localhost:5173`.

## API Endpoints

The backend exposes the following RESTful endpoints:

- `POST /api/auth/register`: Register a new user.
- `POST /api/auth/login`: Authenticate a user and receive a JWT.
- `POST /api/resumes/upload`: Upload a resume file.
- `POST /api/resumes/{resumeId}/analyze`: Trigger an analysis with a job description.
- `GET /api/resumes/history`: Get the analysis history for the logged-in user.

## 🤝 Contributing

Contributions are welcome! Please feel free to open an issue or submit a pull request.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request


