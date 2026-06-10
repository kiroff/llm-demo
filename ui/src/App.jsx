import React, { useState, useEffect, useRef } from 'react';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import { Send, User, Loader2 } from 'lucide-react';
import botSvg from './assets/b.svg';

const API_BASE_URL = '/api/ollama';

const CustomIcon = ({ className, size = 24 }) => (
  <img 
    src={botSvg} 
    alt="B"
    className={className} 
    style={{ width: size, height: size }} 
  />
);

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [conversationId, setConversationId] = useState(() => Math.random().toString(36).substring(7));
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage = { id: Date.now() + '-user', text: input, isBot: false, timestamp: new Date() };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    const botMessageId = Date.now() + '-bot';
    setMessages((prev) => [...prev, { id: botMessageId, text: '', isBot: true, timestamp: new Date() }]);

    try {
      const response = await fetch(`${API_BASE_URL}/stream?question=${encodeURIComponent(input)}&conversationId=${conversationId}`);
      
      if (!response.ok) throw new Error('Network response was not ok');

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let accumulatedText = '';
      let partialLine = '';
      let hasReceivedData = false;

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        const lines = (partialLine + chunk).split('\n');
        partialLine = lines.pop(); // The last element might be incomplete

        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const jsonData = JSON.parse(line.substring(5).trim());
              if (jsonData.chunk) {
                accumulatedText += jsonData.chunk;
                hasReceivedData = true;
                setMessages((prev) => 
                  prev.map((msg) => 
                    msg.id === botMessageId ? { ...msg, text: accumulatedText } : msg
                  )
                );
              }
            } catch (e) {
              console.error('Error parsing chunk:', e, 'Line:', line);
            }
          }
        }
      }
      
      // Handle any remaining partial line
      if (partialLine.startsWith('data:')) {
          try {
              const jsonData = JSON.parse(partialLine.substring(5).trim());
              if (jsonData.chunk) {
                  accumulatedText += jsonData.chunk;
                  hasReceivedData = true;
                  setMessages((prev) =>
                      prev.map((msg) =>
                          msg.id === botMessageId ? { ...msg, text: accumulatedText } : msg
                      )
                  );
              }
          } catch (e) {
              // Ignore incomplete JSON at the end
          }
      }

      if (!hasReceivedData) {
        setMessages((prev) => 
          prev.map((msg) => 
            msg.id === botMessageId ? { ...msg, text: 'No response from model.' } : msg
          )
        );
      }
    } catch (error) {
      console.error('Error fetching response:', error);
      setMessages((prev) => 
        prev.map((msg) => 
          msg.id === botMessageId 
            ? { ...msg, text: 'Sorry, I encountered an error. Is the server running?', isError: true } 
            : msg
        )
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-screen bg-[#84fff3]/10 dark:bg-slate-900 text-slate-900 dark:text-slate-100">
      {/* Header */}
      <header className="bg-white dark:bg-slate-800 shadow-sm p-4 flex items-center gap-2">
        <CustomIcon className="w-8 h-8 text-[#31f7e9]" size={32} />
        <h1 className="text-xl font-bold">Bobson Chat</h1>
      </header>

      {/* Message List */}
      <main className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 && (
          <div className="h-full flex flex-col items-center justify-center text-slate-500 opacity-50">
            <CustomIcon className="mb-2" size={64} />
            <p>Start a conversation with Bobson</p>
          </div>
        )}

        {messages.map((msg, index) => (
          <div
            key={msg.id || index}
            className={`flex ${msg.isBot ? 'justify-start' : 'justify-end'}`}
          >
            <div
              className={`max-w-[80%] p-3 rounded-2xl flex gap-3 ${
                msg.isBot
                  ? 'bg-white dark:bg-slate-800 rounded-tl-none border border-slate-200 dark:border-slate-700'
                  : 'bg-slate-700 text-white font-medium rounded-tr-none'
              } ${msg.isError ? 'border-red-500 text-red-500' : ''}`}
            >
              <div className="mt-1">
                {msg.isBot ? <CustomIcon size={18} /> : <User size={18} />}
              </div>
              {msg.isBot ? (
                <div 
                  className="markdown-content wrap-break-word"
                  dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(marked.parse(msg.text)) }}
                />
              ) : (
                <div className="whitespace-pre-wrap wrap-break-word">{msg.text}</div>
              )}
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="flex justify-start">
            <div className="bg-white dark:bg-slate-800 p-3 rounded-2xl rounded-tl-none border border-slate-200 dark:border-slate-700 flex items-center gap-2">
              <Loader2 className="w-4 h-4 animate-spin" />
              <span className="text-sm">Thinking...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </main>

      {/* Input Area */}
      <footer className="p-4 bg-white dark:bg-slate-800 border-t border-slate-200 dark:border-slate-700">
        <form onSubmit={handleSend} className="max-w-4xl mx-auto flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message..."
            className="flex-1 bg-slate-100 dark:bg-slate-700 border-none rounded-full px-4 py-2 focus:ring-2 focus:bg-slate-700 outline-none"
            disabled={isLoading}
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            className="bg-slate-700 hover:brightness-110 text-shadow-white font-medium p-2 rounded-full disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <Send size={20} />
          </button>
        </form>
      </footer>
    </div>
  );
}

export default App;
