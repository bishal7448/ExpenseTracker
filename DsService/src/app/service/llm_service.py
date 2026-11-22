import os
from pathlib import Path
from dotenv import load_dotenv
from langchain_core.prompts import ChatPromptTemplate
from schema.expense_schema import Expense
from langchain_google_genai import ChatGoogleGenerativeAI

class LLMService:
    def __init__(self):
        # Explicitly load .env from src/.env or current directory
        env_path = Path(__file__).resolve().parent.parent.parent / ".env"
        if env_path.exists():
            load_dotenv(dotenv_path=env_path, override=True)
        else:
            load_dotenv(override=True)

        self.apiKey = os.getenv("GEMINI_API_KEY")
        if self.apiKey:
            os.environ["GOOGLE_API_KEY"] = self.apiKey

        self.prompt = ChatPromptTemplate.from_messages(
            [
                (
                    "system",
                    "You are an expert extraction algorithm."
                    "Only extract relevant information from the text."
                    "If you do not know the value of an attribute asked to extract,"
                    "return null for the attribute's value."
                ),
                ("human", "{text}")
            ]
        )
        self.llm = ChatGoogleGenerativeAI(
            api_key=self.apiKey,
            model="gemini-3.8-flash",
            temperature=0
        )
        self.runable = self.prompt | self.llm.with_structured_output(schema=Expense)

    def runLLM(self, message):
        return self.runable.invoke({"text": message})
