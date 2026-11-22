from utils.message_util import MessageUtil
from service.llm_service import LLMService

class MessageService:
    def __init__(self):
        self.messageUtil = MessageUtil()
        self.llmService = LLMService()

    def process_message(self, message):
        if self.messageUtil.isBankSMS(message):
            return self.llmService.runLLM(message)
        else:
            return None
    