import { useState } from "react";
import { askAssistant } from "./api/assistantApi";
import "./App.css";

type Message = {
  role: "user" | "assistant";
  content: string;
};

function App() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const sendMessage = async () => {
    const question = input.trim();

    if (!question || isLoading) {
      return;
    }

    setMessages((current) => [
      ...current,
      { role: "user", content: question },
    ]);

    setInput("");
    setIsLoading(true);

    try {
      const answer = await askAssistant(question);

      setMessages((current) => [
        ...current,
        { role: "assistant", content: answer },
      ]);
    } catch (error) {
      console.error("Assistant request failed:", error);

      const errorMessage =
        error instanceof Error
          ? error.message
          : "An unexpected error occurred.";

      setMessages((current) => [
        ...current,
        {
          role: "assistant",
          content: `Unable to get a response from LedgerBridge AI.\n\n${errorMessage}`,
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyDown = (
    event: React.KeyboardEvent<HTMLTextAreaElement>,
  ) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      void sendMessage();
    }
  };

  return (
    <div className="app">
      <header className="header">
        <div className="brand">
          <div className="status-indicator" />
          <div>
            <h1>LedgerBridge</h1>
            <p>AI Settlement Assistant</p>
          </div>
        </div>

        <div className="ai-indicator">
          <span className="ai-dot" />
          LedgerBridge AI
        </div>
      </header>

      <main className="chat-container">
        <div className="messages">
          {messages.length === 0 && (
            <div className="welcome">
              <div className="welcome-icon">LB</div>
              <h2>How can I help?</h2>
              <p>
                Ask about transaction status, blockchain settlements, or
                reconciliation.
              </p>
            </div>
          )}

          {messages.map((message, index) => (
            <div
              key={index}
              className={`message ${message.role}`}
            >
              <div className="message-role">
                {message.role === "user" ? "You" : "LedgerBridge AI"}
              </div>

              <div className="message-content">
                {message.content}
              </div>
            </div>
          ))}

          {isLoading && (
            <div className="message assistant">
              <div className="message-role">LedgerBridge AI</div>

              <div className="message-content thinking">
                <span>Thinking</span>
                <span className="thinking-dots">...</span>
              </div>
            </div>
          )}
        </div>

        <div className="input-container">
          <textarea
            value={input}
            placeholder="Ask about a settlement..."
            rows={1}
            disabled={isLoading}
            onChange={(event) => setInput(event.target.value)}
            onKeyDown={handleKeyDown}
          />

          <button
            onClick={() => void sendMessage()}
            disabled={isLoading || !input.trim()}
          >
            {isLoading ? "Thinking..." : "Send"}
          </button>
        </div>

        <div className="input-hint">
          Press Enter to send · Shift+Enter for a new line
        </div>
      </main>
    </div>
  );
}

export default App;