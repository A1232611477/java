<template>
  <div class="portal-figure" aria-hidden="true">
    <svg class="door" viewBox="0 0 220 320" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <radialGradient id="doorGlow" cx="50%" cy="42%" r="62%">
          <stop offset="0%" stop-color="#bcd6ff" stop-opacity="0.95" />
          <stop offset="45%" stop-color="#3b6fd0" stop-opacity="0.45" />
          <stop offset="100%" stop-color="#0a1733" stop-opacity="0" />
        </radialGradient>
        <linearGradient id="goldStroke" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#f6e1a4" />
          <stop offset="100%" stop-color="#b8902f" />
        </linearGradient>
      </defs>
      <path d="M30 312 L30 130 A80 80 0 0 1 190 130 L190 312 Z" fill="url(#doorGlow)" />
      <path d="M46 312 L46 134 A64 64 0 0 1 174 134 L174 312 Z" fill="#06122e" opacity="0.5" />
      <path d="M30 312 L30 130 A80 80 0 0 1 190 130 L190 312" fill="none" stroke="url(#goldStroke)" stroke-width="3" />
      <path d="M46 312 L46 134 A64 64 0 0 1 174 134 L174 312" fill="none" stroke="url(#goldStroke)" stroke-width="1.3" opacity="0.7" />
      <path d="M110 46 l4.5 11 11.5 1 -8.5 8 2.2 11.5 -9.7 -5.7 -9.7 5.7 2.2 -11.5 -8.5 -8 11.5 -1 z" fill="#f6e1a4" />
      <circle cx="92" cy="210" r="1.8" fill="#cfe0ff" />
      <circle cx="132" cy="250" r="1.3" fill="#fff" />
      <circle cx="110" cy="165" r="1.5" fill="#cfe0ff" />
      <circle cx="78" cy="270" r="1.1" fill="#fff" />
    </svg>
    <div class="medallion">祭</div>
  </div>
</template>

<script setup></script>
