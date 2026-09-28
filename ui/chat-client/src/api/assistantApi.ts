export interface AskResponse {
  answer: string;
}

export async function askAssistant(question: string): Promise<string> {
  const response = await fetch(
    "http://localhost:8082/api/v1/assistant/ask",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ question }),
    },
  );

  if (!response.ok) {
    throw new Error(
      `Assistant request failed: ${response.status} ${response.statusText}`,
    );
  }

  const data: AskResponse = await response.json();

  return data.answer;
}