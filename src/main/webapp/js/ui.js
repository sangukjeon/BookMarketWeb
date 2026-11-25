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

/* 주문서: 저장한 배송지 칩을 누르면 받는 분·연락처·주소를 채운다 */
document.addEventListener('click', function (e) {
  var b = e.target.closest && e.target.closest('[data-addr]');
  if (!b) return;
  var set = function (id, v) { var el = document.getElementById(id); if (el) el.value = v || ''; };
  set('receiverName', b.getAttribute('data-name'));
  set('receiverPhone', b.getAttribute('data-phone'));
  set('address', b.getAttribute('data-address'));
  document.querySelectorAll('[data-addr]').forEach(function (x) { x.setAttribute('aria-pressed', x === b ? 'true' : 'false'); });
  document.querySelectorAll('[data-fill]').forEach(function (x) { x.setAttribute('aria-pressed', 'false'); });
});

/* 모바일 장르 칩: 고른 장르가 보이게 가로 스크롤 */
(function () {
  var cur = document.querySelector('.genre-chips [aria-current="true"]');
  if (cur && cur.parentNode.scrollTo) cur.parentNode.scrollTo({ left: cur.offsetLeft - 16 });
})();

/* 검색창 도우미: 누르면 최근 검색어(지우기)와 인기 검색어를 보여 준다 */
(function () {
  var forms = document.querySelectorAll('form[data-suggest]');
  forms.forEach(function (form) {
    var input = form.querySelector('input[name="q"]');
    if (!input) return;
    var panel = document.createElement('div');
    panel.className = 'suggest';
    panel.hidden = true;
    panel.id = (input.id || 'q') + '-suggest';
    form.appendChild(panel);
    input.setAttribute('aria-controls', panel.id);
    input.setAttribute('aria-expanded', 'false');
    input.setAttribute('autocomplete', 'off');

    function link(q) { return form.getAttribute('action') + '?q=' + encodeURIComponent(q); }
    function esc(s) { var d = document.createElement('span'); d.textContent = s; return d.innerHTML; }
    function close() { panel.hidden = true; input.setAttribute('aria-expanded', 'false'); }
    function render(data) {
      var h = '';
      h += '<div class="suggest__group"><div class="suggest__head"><b>최근 검색어</b>' +
        (data.recent.length ? '<button type="button" class="text-btn" data-clear>전체 지우기</button>' : '') + '</div>';
      if (!data.recent.length) h += '<p class="suggest__empty">최근에 찾은 낱말이 없어요</p>';
      else h += '<ul>' + data.recent.map(function (k) {
        return '<li class="suggest__recent"><a href="' + link(k) + '">' + esc(k) + '</a>' +
          '<button type="button" class="suggest__x" data-del="' + esc(k) + '" aria-label="' + esc(k) + ' 지우기">×</button></li>';
      }).join('') + '</ul>';
      h += '</div><div class="suggest__group"><div class="suggest__head"><b>인기 검색어</b><span class="t-help">최근 24시간</span></div>';
      if (!data.popular.length) h += '<p class="suggest__empty">아직 모인 검색어가 없어요</p>';
      else h += '<ol class="suggest__popular">' + data.popular.map(function (k, i) {
        return '<li><a href="' + link(k) + '"><span class="suggest__rank num' + (i < 3 ? ' is-top' : '') + '">' + (i + 1) + '</span>' + esc(k) + '</a></li>';
      }).join('') + '</ol>';
      h += '</div>';
      panel.innerHTML = h;
    }
    function load() {
      fetch(form.getAttribute('data-suggest'), { credentials: 'same-origin' })
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (data) {
          if (!data) return;
          render(data);
          panel.hidden = false;
          input.setAttribute('aria-expanded', 'true');
        }).catch(function () {});
    }
    function remove(keyword) {
      var body = new URLSearchParams();
      body.set('_csrf', form.getAttribute('data-csrf') || '');
      if (keyword) body.set('keyword', keyword);
      fetch(form.getAttribute('data-suggest-delete'), {
        method: 'POST', credentials: 'same-origin', body: body,
        headers: { 'X-Requested-With': 'fetch' }
      }).then(load);
    }
    input.addEventListener('focus', load);
    input.addEventListener('keydown', function (e) { if (e.key === 'Escape') close(); });
    panel.addEventListener('mousedown', function (e) { e.preventDefault(); }); // 누르는 동안 입력창 포커스 유지
    panel.addEventListener('click', function (e) {
      var del = e.target.closest('[data-del]');
      if (del) { remove(del.getAttribute('data-del')); return; }
      if (e.target.closest('[data-clear]')) remove(null);
    });
    input.addEventListener('blur', function () { setTimeout(close, 150); });
  });
})();
