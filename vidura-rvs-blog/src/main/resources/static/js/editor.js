/**
 * editor.js — Quill rich-text editor initialization + localStorage auto-save
 * Loaded only on admin post-form pages.
 */

(function () {
  'use strict';

  // ---- Quill initialisation ------------------------------------------
  const editorEl = document.getElementById('quill-editor');
  const hiddenInput = document.getElementById('content');
  if (!editorEl || !hiddenInput) return;

  const quill = new Quill('#quill-editor', {
    theme: 'snow',
    placeholder: 'Write your post content here…  Use the toolbar to bold text, add headings, lists, links and more.',
    modules: {
      toolbar: [
        [{ 'header': [1, 2, 3, false] }],
        ['bold', 'italic', 'underline', 'strike'],
        ['blockquote', 'code-block'],
        [{ 'list': 'ordered' }, { 'list': 'bullet' }],
        [{ 'indent': '-1' }, { 'indent': '+1' }],
        ['link', 'image'],
        ['clean']
      ]
    }
  });

  // Populate editor with existing content (edit mode)
  if (hiddenInput.value) {
    quill.clipboard.dangerouslyPasteHTML(hiddenInput.value);
  }

  // Sync Quill HTML → hidden textarea before form submit
  const form = editorEl.closest('form');
  if (form) {
    form.addEventListener('submit', () => {
      hiddenInput.value = quill.root.innerHTML;
    });
  }

  // ---- localStorage auto-save ----------------------------------------
  const postId = document.getElementById('postId')?.value;
  const STORAGE_KEY = postId ? `vrvs-draft-${postId}` : 'vrvs-draft-new';
  const banner = document.getElementById('autosave-banner');
  const bannerText = document.getElementById('autosave-text');
  let saveTimer = null;
  let lastSavedContent = quill.root.innerHTML;

  function formatTime(date) {
    const h = date.getHours(), m = date.getMinutes();
    return `${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}`;
  }

  function saveDraft() {
    const html = quill.root.innerHTML;
    if (html === lastSavedContent) return;

    // 1. Always save to localStorage (works offline)
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        content: html,
        savedAt: Date.now()
      }));
    } catch (e) { /* storage full — ignore */ }

    // 2. If editing an existing post, also hit the server autosave endpoint
    if (postId) {
      fetch(`/admin/posts/${postId}/autosave`, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: html,
        credentials: 'same-origin'
      }).catch(() => {}); // silent fail — localStorage is the fallback
    }

    lastSavedContent = html;
    if (banner && bannerText) {
      bannerText.textContent = `Draft saved at ${formatTime(new Date())}`;
      banner.classList.add('visible');
    }
  }

  // Debounce: save 4 seconds after typing stops
  quill.on('text-change', () => {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(saveDraft, 4000);
  });

  // ---- Restore from localStorage on page load (new post only) ---------
  if (!postId) {
    try {
      const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null');
      if (saved && saved.content && saved.content !== '<p><br></p>') {
        const ago = Math.round((Date.now() - saved.savedAt) / 60000);
        const restore = confirm(
          `A draft was saved ${ago > 0 ? ago + ' minutes ago' : 'recently'}. Restore it?`
        );
        if (restore) {
          quill.clipboard.dangerouslyPasteHTML(saved.content);
        }
      }
    } catch (e) {}
  }

  // Clear localStorage draft after successful form submit
  if (form) {
    form.addEventListener('submit', () => {
      try { localStorage.removeItem(STORAGE_KEY); } catch (e) {}
    });
  }

})();
