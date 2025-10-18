// aiScript.js

(() => {
  // ------- DOM elements -------
  const chatBox = document.getElementById('chat-box');
  const sendBtn = document.getElementById('send-btn');
  const userInput = document.getElementById('user-input');
  const menuBtn = document.querySelector('.menu-btn');
  const sidebar = document.querySelector('.sidebar');
  const newChatBtn = document.querySelector('.new-chat-btn');
  const clearChatBtn = document.getElementById('clear-chat');
  const chatHistory = document.getElementById('chat-history');
  const typingIndicatorElem = document.getElementById('typing-indicator');

  // chats: array of chat sessions { id, title, messages: [{text, isUser, timestamp}], createdAt, updatedAt }
  let chats = [];
  let currentChatId = null;

  // ------- Helper: Date and Time -------
  function nowTime() {
    const d = new Date();
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function nowISO() {
    return new Date().toISOString();
  }

  // ------- Save and load from LocalStorage -------
  function saveChats() {
    try {
      localStorage.setItem('chats', JSON.stringify(chats));
    } catch (err) {
      console.error('Error saving chats:', err);
    }
  }

  function loadChatsFromStorage() {
    try {
      const raw = localStorage.getItem('chats');
      if (!raw) return [];
      return JSON.parse(raw);
    } catch (err) {
      console.error('Error loading chats:', err);
      return [];
    }
  }

  // ------- Display/Sort chat list in the sidebar -------
  function renderChatList() {
    chatHistory.innerHTML = '';
    if (!chats || chats.length === 0) {
      const noItem = document.createElement('div');
      noItem.className = 'chat-session';
      noItem.innerHTML = `<i class='bx bx-message-square-dots' style="margin-right: 8px;"></i><span class="text-ellipsis">No conversations</span>`;
      chatHistory.appendChild(noItem);
      return;
    }

    chats.forEach(chat => {
      const chatItem = document.createElement('div');
      chatItem.className = `chat-session ${chat.id === currentChatId ? 'active' : ''}`;
      chatItem.innerHTML = `
        <i class='bx bx-message-square-dots' style="margin-right: 8px;"></i>
        <span class="text-ellipsis">${escapeHtml(chat.title || 'New Chat')}</span>
      `;
      chatItem.addEventListener('click', () => {
        loadChat(chat.id);
        // close sidebar on mobile after selecting session
        if (window.innerWidth <= 768) sidebar.classList.remove('active');
      });
      chatHistory.appendChild(chatItem);
    });
  }

  function createNewChat() {
    const newChat = {
      id: Date.now().toString(),
      title: 'New Chat',
      messages: [],
      createdAt: nowISO(),
      updatedAt: nowISO()
    };
    chats.unshift(newChat);
    currentChatId = newChat.id;
    saveChats();
    renderChatList();
    renderMessagesForCurrentChat();

    // Add a welcome message
    setTimeout(() => {
      appendMessageToCurrentChat("مرحباً! كيف يمكنني مساعدتك اليوم؟", false);
    }, 300);
  }

  function loadChat(chatId) {
    const chat = chats.find(c => c.id === chatId);
    if (!chat) return;
    currentChatId = chatId;
    renderChatList();
    renderMessagesForCurrentChat();
  }

  function renderMessagesForCurrentChat() {
    chatBox.innerHTML = '';
    const chat = chats.find(c => c.id === currentChatId);
    if (!chat) return;

    chat.messages.forEach(m => {
      appendMessageDom(m.text, m.isUser, m.timestamp);
    });

    // Scroll to bottom
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  function appendMessageDom(text, isUser = false, timestamp = null) {
    const messageElement = document.createElement('div');
    messageElement.className = `message ${isUser ? 'user' : 'assistant'}`;

    const avatar = isUser ? 'U' : 'D';
    const sender = isUser ? 'You' : 'Daleel';
    const time = timestamp ? (new Date(timestamp)).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'}) : nowTime();

    // Save text with escape to avoid HTML issues
    const safeText = escapeHtml(text).replace(/\n/g, '<br>');

    messageElement.innerHTML = `
      <div class="message-avatar">${avatar}</div>
      <div class="message-content">
        <div class="message-header">
          <span class="message-sender">${sender}</span>
          <span class="message-time">${time}</span>
        </div>
        <div class="message-text">${safeText}</div>
      </div>
    `;
    chatBox.appendChild(messageElement);
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  function appendMessageToCurrentChat(text, isUser) {
    if (!currentChatId) {
      createNewChat();
    }
    const chat = chats.find(c => c.id === currentChatId);
    if (!chat) return;

    const msg = {
      text: text,
      isUser: !!isUser,
      timestamp: nowISO()
    };
    chat.messages.push(msg);
    chat.updatedAt = nowISO();

    // If it was the first message from the user, update the title
    if (isUser && (!chat.title || chat.title === 'New Chat')) {
      chat.title = text.length > 30 ? text.substring(0, 30) + '...' : text;
    }

    saveChats();
    appendMessageDom(text, isUser, msg.timestamp);
  }

  // Typing indicator
  let typingTimeout = null;
  function showTypingIndicator() {
    if (!typingIndicatorElem) return;
    typingIndicatorElem.classList.add('visible');
    // Ensure scroll to bottom
    chatBox.scrollTop = chatBox.scrollHeight;
  }
  function hideTypingIndicator() {
    if (!typingIndicatorElem) return;
    typingIndicatorElem.classList.remove('visible');
  }

  // AI reply logic
  async function processUserInputAndReply(userMessage) {
    // Show typing indicator
    showTypingIndicator();

    try {
      console.log('🔵 Sending to backend:', userMessage);
      
      // Try different methods to send data
      console.log('🟡 Trying form-urlencoded first...');
      
      // Method 1: form-urlencoded (original)
      let response = await fetch('http://localhost:9090/api/rag', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `userInput=${encodeURIComponent(userMessage)}`
      });
      
      // If that doesn't work, try JSON
      if (!response.ok) {
        console.log('🟡 Form method failed, trying JSON...');
        response = await fetch('http://localhost:9090/api/rag', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            userInput: userMessage
          })
        });
      }
      
      // If still doesn't work, try query parameters
      if (!response.ok) {
        console.log('🟡 JSON method failed, trying query params...');
        response = await fetch(`http://localhost:9090/api/rag?userInput=${encodeURIComponent(userMessage)}`, {
          method: 'GET'
        });
      }

      console.log('🟢 Response status:', response.status);
      console.log('🟢 Response headers:', response.headers);

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      // Get the response as plain text
      const reply = await response.text();
      
      console.log('🔴 Received reply:', reply);
      console.log('🔴 Original question:', userMessage);
      console.log('🔴 Are they identical?', reply === userMessage);
      console.log('🔴 Reply length:', reply.length);
      console.log('🔴 Question length:', userMessage.length);

      // Hide typing indicator
      hideTypingIndicator();

      // Add assistant reply to chat
      appendMessageToCurrentChat(reply, false);

      // Save chat session
      saveChats();

    } catch (error) {
      console.error('Error calling API:', error);
      
      // Hide typing indicator
      hideTypingIndicator();

      // Show error message to user
      const errorMessage = 'عذراً، حدث خطأ في الاتصال بالخدمة. يرجى المحاولة مرة أخرى.';
      appendMessageToCurrentChat(errorMessage, false);
      
      // Save chat session
      saveChats();
    }
  }



  // Send message function
  function sendMessage() {
    const text = (userInput && userInput.value) ? userInput.value.trim() : '';
    if (!text) return;

    // Add user message
    appendMessageToCurrentChat(text, true);

    // Clear input field
    if (userInput) userInput.value = '';

    // Start reply process (typing indicator + default reply or API)
    processUserInputAndReply(text);
  }

  // Load chats on startup or create default chat session
  function initChatSystem() {
    chats = loadChatsFromStorage();
    if (!chats || chats.length === 0) {
      // No saved chats -> create default chat session
      createNewChat();
    } else {
      // Select first chat as open
      currentChatId = chats[0].id;
      renderChatList();
      renderMessagesForCurrentChat();
    }
  }

  // Setup event listeners
  function setupEventListeners() {
    // Send button
    if (sendBtn) {
      sendBtn.addEventListener('click', (e) => {
        e.preventDefault();
        sendMessage();
      });
    }

    // Enter key inside textarea (Shift+Enter for new line)
    if (userInput) {
      userInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
          e.preventDefault();
          sendMessage();
        }
      });
    }

    // Menu button to show/hide sidebar on mobile
    if (menuBtn && sidebar) {
      menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('active');
      });
    }

    // Hide sidebar on mobile when clicking outside (on small screens)
    document.addEventListener('click', (e) => {
      if (window.innerWidth <= 768 && sidebar && !sidebar.contains(e.target) && !menuBtn.contains(e.target)) {
        sidebar.classList.remove('active');
      }
    });

    // New chat button
    if (newChatBtn) {
      newChatBtn.addEventListener('click', () => {
        createNewChat();
        if (window.innerWidth <= 768) sidebar.classList.remove('active');
      });
    }

    //  Clear chats button
    if (clearChatBtn) {
      clearChatBtn.addEventListener('click', () => {
        const ok = confirm('هل أنت متأكد من رغبتك في حذف كل سجل المحادثات؟');
        if (!ok) return;
        localStorage.removeItem('chats');
        chats = [];
        currentChatId = null;
        renderChatList();
        chatBox.innerHTML = '';
        // Create a new default chat session
        createNewChat();
      });
    }

    // Close sidebar on window resize if screen is large enough
    window.addEventListener('resize', () => {
      if (window.innerWidth > 768 && sidebar) {
        sidebar.classList.remove('active');
      }
    });
  }

  function shorten(str, n) {
    if (!str) return '';
    return str.length > n ? str.substring(0, n) + '...' : str;
  }

  // Simple HTML escape to avoid breaking markup
  function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // ------- System Initialization -------
  try {
    setupEventListeners();
    initChatSystem();
    console.log('aiScript initialized');
  } catch (err) {
    console.error('Error initializing aiScript:', err);
  }

})();
