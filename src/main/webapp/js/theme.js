/* 다크 모드 (10장): 고른 값은 localStorage, 처음에는 시스템 설정을 따른다 */
(function () {
  var root = document.documentElement;
  function current() { return root.dataset.theme === 'dark' ? 'dark' : 'light'; }
  function sync() {
    var next = current() === 'dark' ? 'light' : 'dark';
    document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
      // 버튼은 "바뀔 테마"의 아이콘을 보여 준다
      btn.querySelector('use').setAttribute('href', btn.dataset.sprite + (next === 'dark' ? '#i-moon' : '#i-sun'));
      btn.setAttribute('aria-label', next === 'dark' ? '어두운 화면으로 바꾸기' : '밝은 화면으로 바꾸기');
    });
  }
  document.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-theme-toggle]');
    if (!btn) return;
    var next = current() === 'dark' ? 'light' : 'dark';
    if (next === 'dark') root.dataset.theme = 'dark'; else delete root.dataset.theme;
    try { localStorage.setItem('theme', next); } catch (err) {}
    sync();
  });
  document.addEventListener('DOMContentLoaded', sync);
})();
