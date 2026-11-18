/* 토스트 · 하단 시트 · 확인 모달 · 배송지 선택 */
(function () {
  // 토스트 (role=status): 2.5초 뒤 사라진다
  document.addEventListener('DOMContentLoaded', function () {
    var t = document.getElementById('toast');
    if (t) {
      requestAnimationFrame(function () { t.classList.add('show'); });
      setTimeout(function () { t.classList.remove('show'); }, 2500);
    }
  });

  // 하단 시트: 열면 포커스를 안으로, 닫으면 연 버튼으로 돌려준다. Esc로 닫힘
  var opener = null;
  function openSheet(id, from) {
    var s = document.getElementById(id);
    if (!s) return;
    opener = from;
    s.classList.add('open');
    s.setAttribute('aria-hidden', 'false');
    from && from.setAttribute('aria-expanded', 'true');
    var f = s.querySelector('a, button');
    f && f.focus();
  }
  function closeSheets() {
    document.querySelectorAll('.sheet.open').forEach(function (s) {
      s.classList.remove('open');
      s.setAttribute('aria-hidden', 'true');
    });
    if (opener) { opener.setAttribute('aria-expanded', 'false'); opener.focus(); opener = null; }
  }
  document.addEventListener('click', function (e) {
    var o = e.target.closest('[data-sheet-open]');
    if (o) { openSheet(o.dataset.sheetOpen, o); return; }
    if (e.target.closest('[data-sheet-close]') || e.target.classList.contains('sheet-dim')) closeSheets();
  });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeSheets(); });

  // 되돌릴 수 없는 일(비우기·지우기)은 확인 모달을 한 번 띄운다
  var dlg, pending;
  function ensureDialog() {
    if (dlg) return dlg;
    dlg = document.createElement('dialog');
    dlg.className = 'modal';
    dlg.innerHTML = '<h2 class="t-title1" id="mtitle"></h2><p class="t-body3 muted" id="mdesc"></p>' +
      '<div class="modal__actions"><button type="button" class="btn btn-sub" value="no">취소</button>' +
      '<button type="button" class="btn btn-danger" value="yes">확인</button></div>';
    dlg.setAttribute('aria-labelledby', 'mtitle');
    document.body.appendChild(dlg);
    dlg.addEventListener('click', function (e) {
      var b = e.target.closest('button');
      if (!b) return;
      dlg.close();
      if (b.value === 'yes' && pending) { pending.dataset.confirmed = '1'; pending.requestSubmit ? pending.requestSubmit() : pending.submit(); }
    });
    return dlg;
  }
  document.addEventListener('submit', function (e) {
    var f = e.target;
    if (!f.dataset.confirm || f.dataset.confirmed) return;
    e.preventDefault();
    pending = f;
    var d = ensureDialog();
    d.querySelector('#mtitle').textContent = f.dataset.confirm;
    d.querySelector('#mdesc').textContent = f.dataset.confirmDesc || '';
    d.querySelector('[value=yes]').textContent = f.dataset.confirmOk || '확인';
    d.showModal();
    d.querySelector('[value=no]').focus();
  });

  // 주문서: "내 정보와 같아요 / 새로 입력"
  document.addEventListener('click', function (e) {
    var b = e.target.closest('[data-fill]');
    if (!b) return;
    var box = b.closest('[data-shipping]');
    box.querySelectorAll('[data-fill]').forEach(function (x) { x.setAttribute('aria-pressed', String(x === b)); });
    box.querySelectorAll('[data-src]').forEach(function (input) {
      input.value = b.dataset.fill === 'me' ? input.dataset.src : '';
    });
    if (b.dataset.fill === 'new') box.querySelector('[data-src]').focus();
  });
})();

/* 펼침 (로그인 화면의 "아이디" 버튼) */
document.addEventListener('click', function (e) {
  var b = e.target.closest('[data-toggle]');
  if (!b) return;
  var panel = document.getElementById(b.dataset.toggle);
  var open = panel.hidden;
  panel.hidden = !open;
  b.setAttribute('aria-expanded', String(open));
  if (open) { var f = panel.querySelector('input:not([type=hidden])'); f && f.focus(); }
});

/* 별점: 고른 점수를 옆에 "N점"으로 보여 준다 */
document.addEventListener('change', function (e) {
  var r = e.target;
  if (!r.classList || !r.classList.contains('star-input__radio')) return;
  var out = r.closest('.star-field') && r.closest('.star-field').querySelector('.star-field__score');
  if (out) out.textContent = r.value + '점';
});
