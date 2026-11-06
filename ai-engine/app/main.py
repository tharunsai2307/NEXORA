from fastapi import FastAPI, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

from app.api.routes import router
from app.utils.config import settings
from app.utils.logger import setup_logger

logger = setup_logger("main")

app = FastAPI(
    title=settings.SERVICE_NAME,
    version=settings.VERSION,
    description=(
        "FastAPI service for Edge-AI-Based Smart Building Safety and Occupancy Digital Twin System. "
        "Provides microservice endpoints for ML inference, health checks, and anomaly detection."
    ),
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Centralized validation exception handler
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    logger.warning(f"Request validation error on path '{request.url.path}': {exc.errors()}")
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "status": "VALIDATION_ERROR",
            "message": "Invalid request payload schema",
            "details": exc.errors()
        }
    )

# Include API routes
app.include_router(router)


if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting {settings.SERVICE_NAME} on {settings.HOST}:{settings.PORT}...")
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
