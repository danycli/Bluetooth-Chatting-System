// Tab Navigation for Dashboard
function switchTab(tabId) {
  // Update nav buttons
  const navBtns = document.querySelectorAll('.nav-btn');
  navBtns.forEach(btn => btn.classList.remove('active'));
  
  // Find button by onclick parameter content
  const targetBtn = Array.from(navBtns).find(btn => btn.getAttribute('onclick').includes(tabId));
  if (targetBtn) targetBtn.classList.add('active');

  // Update tab contents
  const tabs = document.querySelectorAll('.tab-content');
  tabs.forEach(tab => tab.classList.remove('active'));
  
  const targetTab = document.getElementById(`tab-${tabId}`);
  if (targetTab) targetTab.classList.add('active');
}

// OS Status Clock Synchronization
function updateClock() {
  const now = new Date();
  let hours = now.getHours();
  let minutes = now.getMinutes();
  minutes = minutes < 10 ? '0' + minutes : minutes;
  const timeStr = `${hours}:${minutes}`;
  
  const simClock = document.getElementById('sim-clock');
  if (simClock) simClock.textContent = timeStr;
}

setInterval(updateClock, 1000);
updateClock();

// Global Sim State Variables
let bluetoothOn = true;
let currentSimScreen = 'splash';
let onboardingSlideIndex = 1;
let mockChatMessages = [
  { sender: 'received', text: "Hey there! Just checking if this offline network is working.", time: "12:01 PM" },
  { sender: 'received', text: "Are you receiving these packets?", time: "12:01 PM" },
  { sender: 'sent', text: "Yes, loud and clear! The transmission rate is surprisingly high.", time: "12:02 PM", status: "delivered" },
  { sender: 'received', text: "Awesome. Hey, do you have that map file?", time: "12:02 PM" }
];

// Screen Content Templates for the Simulator Frame
const screenTemplates = {
  splash: () => `
    <div class="screen-splash" onclick="loadSimScreen('onboarding')">
      <div class="splash-logo">
        <svg class="icon-svg lg" viewBox="0 0 24 24"><path fill="var(--mint)" d="M17.71 7.71L12 2h-1v7.59L6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 11 14.41V22h1l5.71-5.71-4.3-4.29 4.3-4.29zM13 5.83l1.88 1.88L13 9.59V5.83zm0 12.34v-3.76l1.88 1.88L13 18.17z"/></svg>
        <span>Aether</span>
      </div>
      <div class="splash-tagline">Chat without the internet.</div>
      <div class="pulse-container">
        <div class="pulse-circle"></div>
        <div class="pulse-circle"></div>
        <div class="pulse-circle"></div>
        <div class="pulse-icon-center">
          <svg class="icon-svg lg" viewBox="0 0 24 24"><path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
        </div>
      </div>
      <div style="margin-top: 32px; font-size: 11px; opacity: 0.5;">Click anywhere to advance</div>
    </div>
  `,
  onboarding: () => `
    <div class="screen-onboarding">
      <div class="onboarding-header">
        <button class="btn-skip" onclick="loadSimScreen('nearby')">Skip</button>
      </div>
      <div class="onboarding-carousel">
        <!-- Slide 1 -->
        <div class="onboarding-slide ${onboardingSlideIndex === 1 ? 'active' : ''}">
          <div class="onboarding-illustration">
            <svg width="160" height="120" viewBox="0 0 160 120" fill="none">
              <rect x="15" y="20" width="36" height="76" rx="8" stroke="var(--latte-dark)" stroke-width="3" fill="var(--white)" />
              <rect x="109" y="20" width="36" height="76" rx="8" stroke="var(--latte-dark)" stroke-width="3" fill="var(--white)" />
              <circle cx="80" cy="58" r="12" stroke="var(--mint)" stroke-dasharray="3 3" stroke-width="2" />
              <circle cx="80" cy="58" r="24" stroke="var(--mint)" stroke-width="2" opacity="0.6"/>
              <circle cx="80" cy="58" r="36" stroke="var(--mint)" stroke-width="1.5" opacity="0.3"/>
            </svg>
          </div>
          <h2 class="onboarding-title">No Wi-Fi? No Problem.</h2>
          <p class="onboarding-desc">Connect and chat with nearby devices directly over Bluetooth. Absolutely zero network or internet required.</p>
        </div>
        
        <!-- Slide 2 -->
        <div class="onboarding-slide ${onboardingSlideIndex === 2 ? 'active' : ''}">
          <div class="onboarding-illustration">
            <svg width="160" height="120" viewBox="0 0 160 120" fill="none">
              <circle cx="80" cy="60" r="40" stroke="var(--mint)" stroke-width="1.5" stroke-dasharray="4 4" />
              <circle cx="80" cy="60" r="20" stroke="var(--mint)" stroke-width="1.5" />
              <circle cx="80" cy="60" r="5" fill="var(--mint)" />
              <circle cx="50" cy="35" r="12" fill="var(--mint-light)" stroke="var(--mint)" stroke-width="1.5" />
              <circle cx="110" cy="85" r="12" fill="var(--mint-light)" stroke="var(--mint)" stroke-width="1.5" />
            </svg>
          </div>
          <h2 class="onboarding-title">Find People Nearby</h2>
          <p class="onboarding-desc">Scan and instantly discover nearby users in emergency zones, campsites, flights, or off-grid areas.</p>
        </div>

        <!-- Slide 3 -->
        <div class="onboarding-slide ${onboardingSlideIndex === 3 ? 'active' : ''}">
          <div class="onboarding-illustration">
            <svg width="160" height="120" viewBox="0 0 160 120" fill="none">
              <rect x="52" y="20" width="56" height="80" rx="10" stroke="var(--latte-dark)" stroke-width="3" fill="var(--white)" />
              <circle cx="80" cy="60" r="20" fill="var(--mint-light)" />
              <path d="M74 60l4 4 8-8" stroke="var(--mint)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>
          <h2 class="onboarding-title">Local & Private</h2>
          <p class="onboarding-desc">Your communications are stored on your device only. Safe from tracking, data leaks, and central failures.</p>
        </div>
      </div>
      
      <div class="onboarding-footer">
        <div class="pagination-dots">
          <div class="dot ${onboardingSlideIndex === 1 ? 'active' : ''}"></div>
          <div class="dot ${onboardingSlideIndex === 2 ? 'active' : ''}"></div>
          <div class="dot ${onboardingSlideIndex === 3 ? 'active' : ''}"></div>
        </div>
        <button class="btn-primary" onclick="nextOnboardingSlide()">
          ${onboardingSlideIndex === 3 ? 'Get Started' : 'Next'}
        </button>
      </div>
    </div>
  `,
  nearby: () => `
    <div class="screen-nearby">
      <div class="app-bar">
        <div class="app-bar-title">Nearby Devices</div>
        <div class="app-bar-actions">
          <div style="display: flex; align-items: center; gap: 6px;">
            <span class="dot-indicator ${bluetoothOn ? 'connected' : 'offline'}"></span>
            <svg class="icon-svg" style="color: ${bluetoothOn ? 'var(--mint)' : 'var(--latte-dark)'};" viewBox="0 0 24 24" onclick="toggleSimBluetooth()"><path d="M17.71 7.71L12 2h-1v7.59L6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 11 14.41V22h1l5.71-5.71-4.3-4.29 4.3-4.29zM13 5.83l1.88 1.88L13 9.59V5.83zm0 12.34v-3.76l1.88 1.88L13 18.17z"/></svg>
          </div>
        </div>
      </div>
      
      <div class="radar-banner">
        <div class="radar-pulse-icon">
          <div class="radar-pulse-ring"></div>
          <div class="radar-pulse-ring"></div>
          <svg class="icon-svg lg" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
        </div>
        <div class="radar-text" style="margin-top: 12px;">Scanning for nearby devices...</div>
      </div>

      <div class="device-list">
        <!-- Device 1 -->
        <div class="device-card" onclick="openPairingModal('Dave\\'s Kindle')">
          <div class="device-card-left">
            <div class="avatar-circle active">
              <span>DK</span>
              <span class="avatar-status-badge connected"></span>
            </div>
            <div class="device-info">
              <div class="device-name">Dave's Kindle</div>
              <div class="device-status-txt">Connected</div>
            </div>
          </div>
          <div class="device-card-right">
            <span class="status-pill connected"><span class="dot-indicator connected"></span>Connected</span>
          </div>
        </div>

        <!-- Device 2 -->
        <div class="device-card" onclick="openPairingModal('Sarah\\'s Pixel')">
          <div class="device-card-left">
            <div class="avatar-circle">
              <span>SP</span>
              <span class="avatar-status-badge"></span>
            </div>
            <div class="device-info">
              <div class="device-name">Sarah's Pixel</div>
              <div class="device-status-txt">Available</div>
            </div>
          </div>
          <div class="device-card-right">
            <span class="status-pill">Available</span>
          </div>
        </div>

        <!-- Device 3 -->
        <div class="device-card" onclick="openPairingModal('OnePlus 11')">
          <div class="device-card-left">
            <div class="avatar-circle">
              <span>OP</span>
              <span class="avatar-status-badge"></span>
            </div>
            <div class="device-info">
              <div class="device-name">OnePlus 11</div>
              <div class="device-status-txt">Available</div>
            </div>
          </div>
          <div class="device-card-right">
            <span class="status-pill">Available</span>
          </div>
        </div>
      </div>

      <button class="fab-btn" onclick="triggerScanFlashAnimation()">
        <svg class="icon-svg lg" viewBox="0 0 24 24"><path d="M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
      </button>
    </div>
  `,
  'chat-list': () => `
    <div class="screen-chat-list">
      <div class="app-bar">
        <div class="app-bar-title">Conversations</div>
        <div class="app-bar-actions">
          <button class="app-bar-btn"><svg class="icon-svg" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></button>
          <button class="app-bar-btn" onclick="loadSimScreen('settings')"><svg class="icon-svg" viewBox="0 0 24 24"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84a.48.48 0 0 0-.48.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96a.488.488 0 0 0-.59.22L3.91 8.87a.49.49 0 0 0 .12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.48-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32a.48.48 0 0 0-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg></button>
        </div>
      </div>

      <div class="search-bar-container">
        <div class="search-input-wrapper">
          <svg class="icon-svg sm" style="opacity: 0.5;" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <input type="text" class="search-input" id="chat-search" placeholder="Search messages..." oninput="filterChatList(this.value)">
        </div>
      </div>

      <div class="chat-list" id="sim-chat-list-container">
        <!-- Chat Item 1 -->
        <div class="chat-item-wrapper" onclick="loadSimScreen('chat-detail')">
          <div class="chat-item-content">
            <div class="chat-item-left">
              <div class="avatar-circle active">
                <span>DK</span>
                <span class="avatar-status-badge connected"></span>
              </div>
              <div class="chat-item-details">
                <span class="chat-item-name">Dave's Kindle</span>
                <span class="chat-item-preview" style="font-weight: 600; color: var(--near-black); opacity: 1;">Hey, do you have that map file?</span>
              </div>
            </div>
            <div class="chat-item-right">
              <span class="chat-item-time" style="color: var(--mint); font-weight: 600;">12:02 PM</span>
              <span class="badge-mint">1</span>
            </div>
          </div>
        </div>

        <!-- Chat Item 2 -->
        <div class="chat-item-wrapper" onclick="loadSimScreen('chat-detail')">
          <div class="chat-item-content">
            <div class="chat-item-left">
              <div class="avatar-circle">
                <span>SP</span>
                <span class="avatar-status-badge connected"></span>
              </div>
              <div class="chat-item-details">
                <span class="chat-item-name">Sarah's Pixel</span>
                <span class="chat-item-preview">Got it, let's meet at building B.</span>
              </div>
            </div>
            <div class="chat-item-right">
              <span class="chat-item-time">11:45 AM</span>
            </div>
          </div>
        </div>

        <!-- Chat Item 3 (Swiped state demo - click left side to open chat, right triggers delete/mute visualization) -->
        <div class="chat-item-wrapper">
          <div class="swipe-actions-bg">
            <button class="swipe-action-btn swipe-action-mute" onclick="alert('Mute clicked')">Mute</button>
            <button class="swipe-action-btn swipe-action-delete" onclick="alert('Delete clicked')">Delete</button>
          </div>
          <div class="chat-item-content swiped" id="swiped-chat-row" onclick="handleChatRowClick(event)">
            <div class="chat-item-left">
              <div class="avatar-circle">
                <span>OP</span>
                <span class="avatar-status-badge"></span>
              </div>
              <div class="chat-item-details">
                <span class="chat-item-name">OnePlus 11</span>
                <span class="chat-item-preview">Can you send the coordinate package again?</span>
              </div>
            </div>
            <div class="chat-item-right">
              <span class="chat-item-time">Yesterday</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  'chat-detail': () => `
    <div class="screen-chat-detail">
      <div class="app-bar">
        <div style="display: flex; align-items: center; gap: var(--spacing-sm);">
          <button class="app-bar-btn" onclick="loadSimScreen('chat-list')">
            <svg class="icon-svg" viewBox="0 0 24 24"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
          </button>
          <div class="chat-detail-header-info">
            <div class="chat-detail-user">
              <span class="chat-detail-name">Dave's Kindle</span>
              <span class="chat-detail-status" style="color: var(--mint);">
                <span class="dot-indicator connected"></span> Connected
              </span>
            </div>
          </div>
        </div>
        <div class="app-bar-actions">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
        </div>
      </div>

      <!-- Optional Reconnecting Banner -->
      <div class="banner-reconnecting" id="sim-reconnecting-banner" style="display: none;">
        <span class="banner-reconnecting-text">
          <span class="banner-reconnecting-spinner"></span>
          Reconnecting...
        </span>
        <span style="opacity:0.6; font-size:10px;">Tap to dismiss</span>
      </div>

      <div class="message-thread" id="sim-message-thread">
        ${renderMessageThread()}
      </div>

      <div class="chat-input-bar">
        <div class="chat-input-field-wrapper">
          <input type="text" class="chat-input-field" id="chat-input-box" placeholder="Message..." onkeypress="handleChatKeyPress(event)">
        </div>
        <button class="chat-send-btn" onclick="sendSimChatMessage()">
          <svg class="icon-svg" viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </div>
    </div>
  `,
  'pairing-modal': () => `
    <div class="screen-nearby">
      <div class="app-bar">
        <div class="app-bar-title">Nearby Devices</div>
      </div>
      <div class="radar-banner" style="filter: blur(1px);">
        <div class="radar-pulse-icon"><svg class="icon-svg lg" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg></div>
        <div class="radar-text" style="margin-top: 12px;">Scanning for nearby devices...</div>
      </div>
      
      <div class="modal-overlay">
        <div class="bottom-sheet" id="sim-pairing-sheet">
          <div class="bottom-sheet-handle"></div>
          <div class="modal-device-icon">
            <svg class="icon-svg lg" viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
          </div>
          <h3 class="modal-title" id="sim-pairing-device-name">Sarah's Pixel</h3>
          <p class="modal-prompt">Connect to this device to begin chatting? This will create a secure, direct channel.</p>
          
          <div class="modal-buttons" id="sim-pairing-buttons-group">
            <button class="btn-primary" onclick="simulatePairingConnection()">Connect</button>
            <button class="btn-secondary" onclick="loadSimScreen('nearby')">Cancel</button>
          </div>
          
          <div class="modal-spinner-container" id="sim-pairing-spinner-group" style="display: none;">
            <div class="mint-spinner"></div>
            <span style="font-size:14px; font-weight:600; color: var(--mint-dark);">Connecting...</span>
          </div>
        </div>
      </div>
    </div>
  `,
  settings: () => `
    <div class="screen-settings">
      <div class="app-bar">
        <div class="app-bar-title">Settings</div>
      </div>
      
      <div class="settings-list">
        <div class="settings-card settings-profile-card">
          <div class="avatar-circle active" style="width: 54px; height: 54px; font-size: 20px;">
            <span>ME</span>
          </div>
          <div class="settings-profile-info">
            <span class="settings-profile-name">My Device</span>
            <span class="settings-profile-tag">Bluetooth visible as "Aether_9a4f"</span>
          </div>
        </div>

        <div class="settings-card" style="display: flex; flex-direction: column; gap: var(--spacing-sm);">
          <div class="settings-row">
            <div class="settings-row-left">
              <div class="settings-row-info">
                <span class="settings-row-title">Discoverability</span>
                <span class="settings-row-desc">Allow nearby devices to find you</span>
              </div>
            </div>
            <label class="switch-control">
              <input type="checkbox" ${bluetoothOn ? 'checked' : ''} onchange="toggleSimBluetoothSwitch(this.checked)">
              <span class="switch-slider"></span>
            </label>
          </div>

          <div class="settings-row">
            <div class="settings-row-left">
              <div class="settings-row-info">
                <span class="settings-row-title">Sound Alerts</span>
                <span class="settings-row-desc">Play chime for incoming messages</span>
              </div>
            </div>
            <label class="switch-control">
              <input type="checkbox" checked>
              <span class="switch-slider"></span>
            </label>
          </div>
        </div>

        <div class="settings-card" style="display: flex; flex-direction: column;">
          <div class="settings-row" onclick="wipeLocalVault()">
            <div class="settings-row-left">
              <div class="settings-row-info">
                <span class="settings-row-title" style="color: #c0392b;">Clear Data Vault</span>
                <span class="settings-row-desc">Permanently wipe local conversation stores</span>
              </div>
            </div>
          </div>
          <div class="settings-row" style="border: none;">
            <div class="settings-row-left">
              <div class="settings-row-info">
                <span class="settings-row-title">About Aether</span>
                <span class="settings-row-desc">Version 1.0.4 (Offline Mesh Release)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  'status-states': () => `
    <div class="screen-settings" style="background-color: var(--ice-latte); height: 100%;">
      <div class="app-bar"><div class="app-bar-title">Connection Matrix</div></div>
      
      <div class="settings-list">
        <div class="settings-card" style="display: flex; flex-direction: column; gap: 16px;">
          <h4 style="border-bottom: 1px solid var(--ice-latte); padding-bottom: 8px;">Pills & Badges</h4>
          
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <div>
              <div style="font-size: 11px; font-weight:600; color: var(--latte-dark); margin-bottom: 4px;">Connected Pill</div>
              <span class="status-pill connected">
                <span class="dot-indicator connected"></span>Connected
              </span>
            </div>

            <div>
              <div style="font-size: 11px; font-weight:600; color: var(--latte-dark); margin-bottom: 4px;">Connecting Pill (Pulsing)</div>
              <span class="status-pill connecting">
                <span class="dot-indicator connecting"></span>Connecting...
              </span>
            </div>

            <div>
              <div style="font-size: 11px; font-weight:600; color: var(--latte-dark); margin-bottom: 4px;">Disconnected Pill (Red Dot)</div>
              <span class="status-pill disconnected">
                <span class="dot-indicator disconnected"></span>Disconnected
              </span>
            </div>

            <div>
              <div style="font-size: 11px; font-weight:600; color: var(--latte-dark); margin-bottom: 4px;">Bluetooth Disabled (Gray Dot)</div>
              <span class="status-pill disabled">
                <span class="dot-indicator offline"></span>Bluetooth is off, tap to enable
              </span>
            </div>
          </div>
        </div>

        <div class="settings-card" style="display: flex; flex-direction: column; gap: 12px;">
          <h4 style="border-bottom: 1px solid var(--ice-latte); padding-bottom: 8px;">Context Subtitles</h4>
          
          <div>
            <span style="font-size: 13px; font-weight:600; display:flex; align-items:center; gap: 4px; color: var(--mint);">
              <span class="dot-indicator connected"></span> Connected
            </span>
          </div>
          
          <div>
            <span style="font-size: 13px; font-weight:600; display:flex; align-items:center; gap: 4px; color: var(--near-black); opacity: 0.6;">
              <span class="dot-indicator disconnected"></span> Disconnected
            </span>
          </div>
        </div>
      </div>
    </div>
  `
};

// Render Screen template into the simulator frame
function loadSimScreen(screenId) {
  currentSimScreen = screenId;
  onboardingSlideIndex = 1; // Reset slide carousel
  
  // Highlight active menu item
  const listItems = document.querySelectorAll('.screen-select-item');
  listItems.forEach(item => {
    item.classList.remove('active');
    if (item.getAttribute('onclick').includes(screenId)) {
      item.classList.add('active');
    }
  });

  const screenContainer = document.getElementById('sim-screen-container');
  if (screenContainer) {
    screenContainer.innerHTML = screenTemplates[screenId]();
    // Scroll thread to bottom if loading chat screen
    if (screenId === 'chat-detail') {
      scrollChatToBottom();
    }
  }
}

// Onboarding Navigation
function nextOnboardingSlide() {
  if (onboardingSlideIndex < 3) {
    onboardingSlideIndex++;
    const screenContainer = document.getElementById('sim-screen-container');
    if (screenContainer) {
      screenContainer.innerHTML = screenTemplates.onboarding();
    }
  } else {
    loadSimScreen('nearby');
  }
}

// Toggle Bluetooth State
function toggleSimBluetooth() {
  bluetoothOn = !bluetoothOn;
  const btIcon = document.getElementById('sim-status-bt');
  if (btIcon) {
    btIcon.style.color = bluetoothOn ? 'var(--mint)' : 'var(--latte-dark)';
  }
  // Reload current screen if it displays Bluetooth state
  if (currentSimScreen === 'nearby' || currentSimScreen === 'settings') {
    loadSimScreen(currentSimScreen);
  }
}

function toggleSimBluetoothSwitch(checked) {
  bluetoothOn = checked;
  const btIcon = document.getElementById('sim-status-bt');
  if (btIcon) {
    btIcon.style.color = bluetoothOn ? 'var(--mint)' : 'var(--latte-dark)';
  }
}

// Pairing sheet actions
let pairingDeviceName = '';
function openPairingModal(deviceName) {
  pairingDeviceName = deviceName;
  loadSimScreen('pairing-modal');
  const label = document.getElementById('sim-pairing-device-name');
  if (label) label.textContent = deviceName;
}

function simulatePairingConnection() {
  const btnGroup = document.getElementById('sim-pairing-buttons-group');
  const spinnerGroup = document.getElementById('sim-pairing-spinner-group');
  
  if (btnGroup && spinnerGroup) {
    btnGroup.style.display = 'none';
    spinnerGroup.style.display = 'flex';
    
    // Simulate connection duration
    setTimeout(() => {
      loadSimScreen('chat-detail');
    }, 1800);
  }
}

// Scan Refresher Animation
function triggerScanFlashAnimation() {
  const rings = document.querySelectorAll('.radar-pulse-ring');
  rings.forEach(ring => {
    ring.style.animation = 'none';
    setTimeout(() => {
      ring.style.animation = '';
    }, 10);
  });
}

// Swiped Conversation layout handler
function handleChatRowClick(event) {
  const swipedRow = document.getElementById('swiped-chat-row');
  if (swipedRow) {
    // If user clicks on the mute/delete side, don't open chat
    if (event.target.closest('.swipe-actions-bg')) return;
    
    // Check if row is currently swiped left
    if (swipedRow.classList.contains('swiped')) {
      swipedRow.classList.remove('swiped');
    } else {
      loadSimScreen('chat-detail');
    }
  }
}

// Chat functions
function renderMessageThread() {
  return mockChatMessages.map(msg => `
    <div class="message-group">
      <div class="message-bubble-wrapper ${msg.sender}">
        <div class="message-bubble ${msg.sender}">
          ${msg.text}
        </div>
      </div>
      <div class="message-meta">
        ${msg.time} ${msg.status ? `&bull; ${msg.status === 'delivered' ? 'Delivered' : 'Sent'}` : ''}
        ${msg.sender === 'sent' ? `
          <svg class="icon-svg sm" style="color: var(--mint); margin-left: 2px;" viewBox="0 0 24 24"><path d="M0 0h24v24H0z" fill="none"/><path d="M18 7l-1.41-1.41-6.34 6.34 1.41 1.41L18 7zm4.24-1.41L11.66 16.17 7.48 12l-1.41 1.41L11.66 19l12-12-1.42-1.41zM2 12l1.41-1.41L9 16.17l-1.41 1.41L2 12z"/></svg>
        ` : ''}
      </div>
    </div>
  `).join('');
}

function scrollChatToBottom() {
  const thread = document.getElementById('sim-message-thread');
  if (thread) {
    thread.scrollTop = thread.scrollHeight;
  }
}

function handleChatKeyPress(event) {
  if (event.key === 'Enter') {
    sendSimChatMessage();
  }
}

function sendSimChatMessage() {
  const input = document.getElementById('chat-input-box');
  if (input && input.value.trim() !== '') {
    const text = input.value.trim();
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    mockChatMessages.push({
      sender: 'sent',
      text: text,
      time: time,
      status: 'delivered'
    });
    
    input.value = '';
    
    const thread = document.getElementById('sim-message-thread');
    if (thread) {
      thread.innerHTML = renderMessageThread();
      scrollChatToBottom();
    }
  }
}

// Control simulation externally
function simulateReceivedMessage() {
  if (currentSimScreen !== 'chat-detail') {
    // If not on chat detail, redirect first
    loadSimScreen('chat-detail');
  }
  
  setTimeout(() => {
    const responses = [
      "Let's test file transfer next.",
      "Are you close by? Signal shows 4 bars.",
      "Got it. Bringing the supplies now.",
      "The connection is steady. 0% packet drop.",
      "Aether network is active!"
    ];
    const text = responses[Math.floor(Math.random() * responses.length)];
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    mockChatMessages.push({
      sender: 'received',
      text: text,
      time: time
    });
    
    const thread = document.getElementById('sim-message-thread');
    if (thread) {
      thread.innerHTML = renderMessageThread();
      scrollChatToBottom();
    }
  }, 300);
}

// Reconnecting banner toggle
function toggleSimReconnectingBanner() {
  if (currentSimScreen !== 'chat-detail') {
    loadSimScreen('chat-detail');
  }
  setTimeout(() => {
    const banner = document.getElementById('sim-reconnecting-banner');
    if (banner) {
      if (banner.style.display === 'none') {
        banner.style.display = 'flex';
      } else {
        banner.style.display = 'none';
      }
    }
  }, 100);
}

// Filter Chat List
function filterChatList(query) {
  const items = document.querySelectorAll('#sim-chat-list-container .chat-item-wrapper');
  items.forEach(item => {
    const name = item.querySelector('.chat-item-name').textContent.toLowerCase();
    const preview = item.querySelector('.chat-item-preview').textContent.toLowerCase();
    if (name.includes(query.toLowerCase()) || preview.includes(query.toLowerCase())) {
      item.style.display = 'block';
    } else {
      item.style.display = 'none';
    }
  });
}

function wipeLocalVault() {
  if (confirm("Are you sure you want to permanently delete all local messaging databases and keys? This cannot be undone.")) {
    mockChatMessages = [];
    alert("Vault wiped successfully. Restarting simulator.");
    loadSimScreen('splash');
  }
}

// Initial Screen Bootup inside simulator
window.onload = () => {
  loadSimScreen('splash');
};
