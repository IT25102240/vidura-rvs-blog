/**
 * main.js — ViduraRvs public-site interactivity
 * - Reading progress bar
 * - Image lightbox
 * - Social share buttons
 * - Smooth reveal animations (IntersectionObserver)
 */

(function () {
  'use strict';

  // ---- Reading Progress Bar ------------------------------------------
  const progressBar = document.getElementById('reading-progress');
  if (progressBar) {
    const updateProgress = () => {
      const scrollTop = window.scrollY;
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      const pct = docHeight > 0 ? Math.min(100, (scrollTop / docHeight) * 100) : 0;
      progressBar.style.width = pct + '%';
    };
    window.addEventListener('scroll', updateProgress, { passive: true });
    updateProgress();
  }

  // ---- Image Lightbox ------------------------------------------------
  const lightbox = document.getElementById('lightbox');
  const lightboxImg = document.getElementById('lightbox-img');

  if (lightbox && lightboxImg) {
    document.querySelectorAll('.post-gallery img').forEach(img => {
      img.style.cursor = 'zoom-in';
      img.addEventListener('click', () => {
        lightboxImg.src = img.src;
        lightboxImg.alt = img.alt;
        lightbox.classList.add('active');
        document.body.style.overflow = 'hidden';
      });
    });

    const closeLightbox = () => {
      lightbox.classList.remove('active');
      document.body.style.overflow = '';
    };

    document.getElementById('lightbox-close')?.addEventListener('click', closeLightbox);
    lightbox.addEventListener('click', e => { if (e.target === lightbox) closeLightbox(); });
    document.addEventListener('keydown', e => { if (e.key === 'Escape') closeLightbox(); });
  }

  // ---- Social Share Buttons ------------------------------------------
  document.querySelectorAll('[data-share]').forEach(btn => {
    btn.addEventListener('click', e => {
      e.preventDefault();
      const type = btn.dataset.share;
      const url = encodeURIComponent(window.location.href);
      const title = encodeURIComponent(document.title);

      if (type === 'x') {
        window.open(`https://twitter.com/intent/tweet?text=${title}&url=${url}`, '_blank', 'width=600,height=400');
      } else if (type === 'whatsapp') {
        window.open(`https://wa.me/?text=${title}%20${url}`, '_blank');
      } else if (type === 'copy') {
        navigator.clipboard.writeText(window.location.href).then(() => {
          const orig = btn.textContent;
          btn.textContent = '✓ Copied!';
          btn.style.background = 'var(--success)';
          setTimeout(() => {
            btn.textContent = orig;
            btn.style.background = '';
          }, 2000);
        }).catch(() => {
          // Fallback for older browsers
          const ta = document.createElement('textarea');
          ta.value = window.location.href;
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
        });
      }
    });
  });

  // ---- Card reveal animations (IntersectionObserver) -----------------
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.style.opacity = '1';
          entry.target.style.transform = 'translateY(0)';
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1 });

    document.querySelectorAll('.post-card, .stat-card, .related-card').forEach(card => {
      card.style.opacity = '0';
      card.style.transform = 'translateY(20px)';
      card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
      observer.observe(card);
    });
  }

  // ---- Image upload preview (admin post form) -------------------------
  const imageInputs = document.querySelectorAll('.image-file-input');
  imageInputs.forEach((input, idx) => {
    input.addEventListener('change', () => {
      const file = input.files[0];
      if (!file) return;
      const previewId = input.dataset.preview;
      const previewEl = document.getElementById(previewId);
      if (!previewEl) return;
      const reader = new FileReader();
      reader.onload = e => {
        previewEl.innerHTML = `
          <div class="image-preview-item">
            <img src="${e.target.result}" alt="Preview">
            ${idx === 0 ? '<span class="image-preview-badge">Cover</span>' : ''}
          </div>`;
      };
      reader.readAsDataURL(file);
    });
  });

})();
