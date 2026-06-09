import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Send, User, Loader2 } from 'lucide-react';
import botSvg from './assets/b.svg';

const API_BASE_URL = 'http://localhost:8181/api/ollama';

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

    const userMessage = { text: input, isBot: false, timestamp: new Date() };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await axios.get(`${API_BASE_URL}/questions/${encodeURIComponent(input)}`);
      const botMessage = { text: response.data, isBot: true, timestamp: new Date() };
      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error('Error fetching response:', error);
      const errorMessage = { 
        text: 'Sorry, I encountered an error. Is the Bobson server running?', 
        isBot: true, 
        isError: true,
        timestamp: new Date() 
      };
      setMessages((prev) => [...prev, errorMessage]);
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
            key={index}
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
              <div className="whitespace-pre-wrap break-words">{msg.text}</div>
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
