/* Copyright (c) 2026. Jericho Crosby (Chalwk) */

(function () {
    'use strict';

    // Small helpers so the rest of the file does not repeat document.querySelector.
    const $ = (sel, root) => (root || document).querySelector(sel);
    const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));

    // Used for anything that goes back into innerHTML.
    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    // Fires a short message at the bottom right. The timeout is cleared so fast clicks do not stack.
    function toast(msg, isError) {
        const el = $('#editor-toast');
        if (!el) return;
        el.textContent = msg;
        el.classList.toggle('is-error', !!isError);
        el.classList.add('is-visible');
        clearTimeout(el._t);
        el._t = setTimeout(() => el.classList.remove('is-visible'), 2400);
    }

    // Builds a temporary blob URL and clicks a hidden link.
    function download(filename, text) {
        const blob = new Blob([text], { type: 'text/yaml;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    }

    // Nexus stores users by UUID when it can, but older files may still be keyed by name.
    const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

    // Permission strings can look like node, -node, or node;1234567890. That last part is an expiry in epoch milliseconds.
    function parsePermEntry(str) {
        let s = String(str).trim();
        let value = true;
        if (s.startsWith('-')) { value = false; s = s.substring(1); }
        let expires = 0;
        const semi = s.indexOf(';');
        if (semi > 0) {
            const n = Number(s.substring(semi + 1).trim());
            if (Number.isFinite(n) && n > 0) {
                expires = n;
                s = s.substring(0, semi);
            }
        }
        return { node: s.trim(), value, expires };
    }

    // Turn the editor object back into the compact string Nexus writes.
    function serializePermEntry(p) {
        let s = (p.value ? '' : '-') + p.node;
        if (p.expires > 0) s += ';' + p.expires;
        return s;
    }

    // YAML can be missing sections, so every subject starts with empty defaults.
    function normalizeSubject(raw) {
        const out = { options: {}, parents: [], permissions: [], contexts: {} };
        if (!raw || typeof raw !== 'object') return out;
        if (raw.options && typeof raw.options === 'object') {
            for (const [k, v] of Object.entries(raw.options)) {
                out.options[k] = v == null ? '' : String(v);
            }
        }
        if (Array.isArray(raw.parents)) out.parents = raw.parents.map(String);
        if (Array.isArray(raw.permissions)) out.permissions = raw.permissions.map(parsePermEntry);
        if (raw.contexts && typeof raw.contexts === 'object') {
            for (const [ctx, list] of Object.entries(raw.contexts)) {
                out.contexts[ctx] = Array.isArray(list) ? list.map(parsePermEntry) : [];
            }
        }
        return out;
    }

    // Only write keys that actually have content. Empty parents arrays are still kept because Nexus expects them on groups.
    function buildSubjectOut(sub) {
        const out = {};
        if (Object.keys(sub.options).length > 0) {
            const opts = {};
            for (const [k, v] of Object.entries(sub.options)) {
                if (/^-?\d+$/.test(String(v).trim())) opts[k] = parseInt(v, 10);
                else opts[k] = v;
            }
            out.options = opts;
        }
        if (sub.permissions.length > 0) out.permissions = sub.permissions.map(serializePermEntry);
        if (Object.keys(sub.contexts).length > 0) {
            out.contexts = {};
            for (const [ctx, list] of Object.entries(sub.contexts)) {
                if (list.length > 0) out.contexts[ctx] = list.map(serializePermEntry);
            }
        }
        out.parents = [...sub.parents];
        return out;
    }

    // Top level groups map. Anything missing gets filled in by normalizeSubject.
    function loadGroupsYaml(text) {
        const doc = window.jsyaml.load(text) || {};
        const groups = {};
        for (const [name, raw] of Object.entries(doc.groups || {})) {
            groups[name] = normalizeSubject(raw);
        }
        return groups;
    }

    // Users can be keyed by UUID or by name. The name field is kept around for readability.
    function loadUsersYaml(text) {
        const doc = window.jsyaml.load(text) || {};
        const users = {};
        for (const [key, raw] of Object.entries(doc.users || {})) {
            const subject = normalizeSubject(raw);
            const name = (raw && raw.name) ? String(raw.name) : key;
            users[key] = {
                key,
                uuid: UUID_RE.test(key) ? key : null,
                name,
                ...subject,
            };
        }
        return users;
    }

    // Sort groups for stable diffs. The header comment matches the one Nexus writes.
    function writeGroupsYaml(groups) {
        const data = { groups: {} };
        for (const name of Object.keys(groups).sort()) {
            data.groups[name] = buildSubjectOut(groups[name]);
        }
        const header =
            '# NexusPermissions Groups\n' +
            '#\n' +
            '# Permissions prefixed with "-" are negated. Lower weight = higher priority.\n' +
            '# Context-specific permissions go under "contexts".\n' +
            '#\n' +
            '# Edited with the NexusPermissions YAML Editor.\n\n';
        return header + window.jsyaml.dump(data, {
            indent: 2, lineWidth: 120, noRefs: true, sortKeys: false,
        });
    }

    // Skip users that have nothing but a name. They do not need an entry in users.yml.
    function writeUsersYaml(users) {
        const data = { users: {} };
        for (const key of Object.keys(users).sort()) {
            const u = users[key];
            const entry = buildSubjectOut(u);
            if (u.uuid && u.name && u.name !== u.key) entry.name = u.name;

            const hasContent =
                (entry.permissions && entry.permissions.length) ||
                (entry.parents && entry.parents.length) ||
                (entry.options && Object.keys(entry.options).length) ||
                (entry.contexts && Object.keys(entry.contexts).length);
            if (!hasContent) continue;

            data.users[key] = entry;
        }
        const header =
            '# NexusPermissions Users\n' +
            '#\n' +
            '# Keyed by UUID when available, otherwise by name. Only users with\n' +
            '# specific modifications are stored here.\n' +
            '#\n' +
            '# Edited with the NexusPermissions YAML Editor.\n\n';
        return header + window.jsyaml.dump(data, {
            indent: 2, lineWidth: 120, noRefs: true, sortKeys: false,
        });
    }

    // All editor state lives here. The render functions read from this and never touch the DOM directly for data.
    const state = {
        groups: {},
        users: {},
        activeTab: 'groups',
        activeGroup: null,
        activeUser: null,
        groupsLoaded: false,
        usersLoaded: false,
    };

    function currentSubject() {
        if (state.activeTab === 'groups' && state.activeGroup) {
            return state.groups[state.activeGroup];
        }
        if (state.activeTab === 'users' && state.activeUser) {
            return state.users[state.activeUser];
        }
        return null;
    }

    function currentKey() {
        return state.activeTab === 'groups' ? state.activeGroup : state.activeUser;
    }

    // The two file cards at the top. They also act as drop targets.
    function renderFiles() {
        const el = $('#editor-files');
        if (!el) return;

        el.innerHTML = `
            ${fileCard('groups', 'groups.yml', state.groupsLoaded,
            state.groupsLoaded ? Object.keys(state.groups).length + ' groups' : 'Not loaded',
            'Groups, weights, permissions, contexts, parents.')}
            ${fileCard('users', 'users.yml', state.usersLoaded,
                state.usersLoaded ? Object.keys(state.users).length + ' users' : 'Not loaded',
                'Per-user permissions, parents, options and contexts.')}
        `;
    }

    function fileCard(kind, name, loaded, status, desc) {
        return `
            <div class="file-card ${loaded ? 'is-loaded' : ''}" data-drop="${kind}">
                <div class="file-card-head">
                    <h3 class="file-card-name">${name}</h3>
                    <span class="file-card-status">${escapeHtml(status)}</span>
                </div>
                <p class="file-card-desc">${desc}</p>
                <div class="file-card-actions">
                    <button class="btn btn-sm btn-secondary" data-action="pick-${kind}">
                        ${loaded ? 'Replace' : 'Choose file'}
                    </button>
                    ${loaded ? `
                        <button class="btn btn-sm btn-primary" data-action="download-${kind}">Download</button>
                        <button class="btn btn-sm btn-ghost" data-action="clear-${kind}">Clear</button>
                    ` : ''}
                </div>
                <input type="file" class="file-input" accept=".yml,.yaml,text/yaml,text/plain"
                       data-input="${kind}">
            </div>
        `;
    }

    function renderWorkspace() {
        const ws = $('#editor-workspace');
        const empty = $('#editor-empty');
        const hasAny = state.groupsLoaded || state.usersLoaded;
        ws.hidden = !hasAny;
        if (empty) empty.style.display = hasAny ? 'none' : '';
        if (!hasAny) return;

        renderTabs();
        renderSplit();
    }

    function renderTabs() {
        const el = $('#editor-tabs');
        if (!el) return;
        const groups = state.groupsLoaded ? Object.keys(state.groups).length : null;
        const users = state.usersLoaded ? Object.keys(state.users).length : null;
        el.innerHTML = `
            <button class="editor-tab ${state.activeTab === 'groups' ? 'is-active' : ''}"
                    data-action="tab" data-tab="groups"
                    ${state.groupsLoaded ? '' : 'disabled'}>
                Groups
                ${groups != null ? `<span class="editor-tab-count">${groups}</span>` : ''}
            </button>
            <button class="editor-tab ${state.activeTab === 'users' ? 'is-active' : ''}"
                    data-action="tab" data-tab="users"
                    ${state.usersLoaded ? '' : 'disabled'}>
                Users
                ${users != null ? `<span class="editor-tab-count">${users}</span>` : ''}
            </button>
        `;
    }

    function renderSplit() {
        const el = $('#editor-split');
        if (!el) return;
        el.innerHTML = renderList() + renderPane();
    }

    function renderList() {
        if (state.activeTab === 'groups') return renderGroupList();
        return renderUserList();
    }

    // Groups are sorted by weight so the list matches how Nexus resolves them.
    function renderGroupList() {
        const names = Object.keys(state.groups).sort((a, b) => {
            const wa = parseInt(state.groups[a].options.weight || 1000, 10);
            const wb = parseInt(state.groups[b].options.weight || 1000, 10);
            return wa - wb || a.localeCompare(b);
        });

        let body = '';
        if (names.length === 0) {
            body = '<p class="text-muted" style="padding:8px;font-size:0.85rem;">No groups.</p>';
        } else {
            for (const name of names) {
                const g = state.groups[name];
                const w = g.options.weight != null ? g.options.weight : '1000';
                const active = name === state.activeGroup ? 'is-active' : '';
                body += `
                    <button class="editor-list-item ${active}"
                            data-action="select-group" data-name="${escapeHtml(name)}">
                        <span class="editor-list-item-name">${escapeHtml(name)}</span>
                        <span class="editor-list-item-badge">${escapeHtml(w)}</span>
                    </button>
                `;
            }
        }
        return `
            <aside class="editor-list">
                <div class="editor-list-head">
                    <h2 class="editor-list-title">Groups</h2>
                    <button class="icon-btn" title="New group" data-action="new-group">+</button>
                </div>
                ${body}
            </aside>
        `;
    }

    function renderUserList() {
        const keys = Object.keys(state.users).sort((a, b) => {
            const na = state.users[a].name || a;
            const nb = state.users[b].name || b;
            return na.localeCompare(nb);
        });

        let body = '';
        if (keys.length === 0) {
            body = '<p class="text-muted" style="padding:8px;font-size:0.85rem;">No users.</p>';
        } else {
            for (const key of keys) {
                const u = state.users[key];
                const active = key === state.activeUser ? 'is-active' : '';
                body += `
                    <button class="editor-list-item ${active}"
                            data-action="select-user" data-key="${escapeHtml(key)}">
                        <span class="editor-list-item-name">${escapeHtml(u.name || key)}</span>
                        <span class="editor-list-item-badge">${u.uuid ? 'uuid' : 'name'}</span>
                    </button>
                `;
            }
        }
        return `
            <aside class="editor-list">
                <div class="editor-list-head">
                    <h2 class="editor-list-title">Users</h2>
                    <button class="icon-btn" title="New user" data-action="new-user">+</button>
                </div>
                ${body}
            </aside>
        `;
    }

    function renderPane() {
        const sub = currentSubject();
        if (!sub) {
            const verb = state.activeTab === 'groups' ? 'group' : 'user';
            return `
                <div class="editor-pane">
                    <div class="editor-pane-empty">
                        Select a ${verb} on the left, or create a new one.
                    </div>
                </div>
            `;
        }
        return `<div class="editor-pane">${renderSubjectEditor(sub)}</div>`;
    }

    function renderSubjectEditor(sub) {
        const isGroup = state.activeTab === 'groups';
        const key = currentKey();
        const displayName = isGroup ? key : (sub.name || key);

        let html = '';
        html += '<div class="editor-pane-head">';
        html += `<h2 class="editor-pane-title">${escapeHtml(displayName)}</h2>`;
        html += '<div style="display:flex;gap:8px;">';
        if (isGroup) {
            html += `<button class="btn btn-sm btn-danger" data-action="delete-group" data-name="${escapeHtml(key)}">Delete group</button>`;
        } else {
            html += `<button class="btn btn-sm btn-danger" data-action="delete-user" data-key="${escapeHtml(key)}">Delete user</button>`;
        }
        html += '</div></div>';

        if (isGroup) {
            html += `
                <div class="form-row">
                    <label>Name</label>
                    <input type="text" value="${escapeHtml(key)}" disabled>
                </div>
            `;
        } else {
            html += `
                <div class="form-row">
                    <label>Name</label>
                    <input type="text" data-field="user-name" value="${escapeHtml(sub.name || '')}"
                           placeholder="Display name">
                </div>
            `;
            if (sub.uuid) {
                html += `
                    <div class="form-row">
                        <label>UUID</label>
                        <input type="text" value="${escapeHtml(sub.uuid)}" disabled>
                    </div>
                `;
            }
        }

        if (isGroup) {
            const w = sub.options.weight != null ? sub.options.weight : '1000';
            html += `
                <div class="form-row">
                    <label>Weight</label>
                    <input type="number" data-field="weight" value="${escapeHtml(w)}" min="0" step="1">
                </div>
            `;
        }

        const prefix = sub.options.prefix != null ? sub.options.prefix : '';
        html += `
            <div class="form-row">
                <label>Prefix</label>
                <input type="text" data-field="prefix" value="${escapeHtml(prefix)}"
                       placeholder="&amp;7">
            </div>
        `;

        html += renderOptionsSection(sub);
        html += renderParentsSection(sub);
        html += renderPermissionsSection(sub, 'global');
        html += renderContextsSection(sub);

        return html;
    }

    function renderOptionsSection(sub) {
        const entries = Object.entries(sub.options)
            .filter(([k]) => k !== 'weight' && k !== 'prefix');

        let rows = '';
        if (entries.length === 0) {
            rows = '<p class="text-muted" style="font-size:0.85rem;margin:0;">No extra options.</p>';
        } else {
            for (const [k, v] of entries) {
                rows += `
                    <div class="option-row">
                        <input type="text" data-field="option-key" data-old="${escapeHtml(k)}"
                               value="${escapeHtml(k)}">
                        <input type="text" data-field="option-value" data-key="${escapeHtml(k)}"
                               value="${escapeHtml(v)}">
                        <button class="icon-btn is-danger" title="Remove"
                                data-action="remove-option" data-key="${escapeHtml(k)}">×</button>
                    </div>
                `;
            }
        }

        return `
            <section class="editor-section">
                <div class="editor-section-head">
                    <h3 class="editor-section-title">Options</h3>
                    <button class="btn btn-sm btn-ghost" data-action="add-option">+ Add option</button>
                </div>
                ${rows}
            </section>
        `;
    }

    function renderParentsSection(sub) {
        const allNames = Object.keys(state.groups).sort();
        const others = allNames.filter(n => n !== currentKey());

        let grid = '';
        if (others.length === 0) {
            grid = '<p class="text-muted" style="font-size:0.85rem;margin:0;">No other groups to inherit from.</p>';
        } else {
            for (const name of others) {
                const checked = sub.parents.includes(name) ? 'checked' : '';
                const cls = sub.parents.includes(name) ? 'is-checked' : '';
                grid += `
                    <label class="parent-chip ${cls}">
                        <input type="checkbox" data-field="parent" value="${escapeHtml(name)}" ${checked}>
                        <span>${escapeHtml(name)}</span>
                    </label>
                `;
            }
        }

        return `
            <section class="editor-section">
                <div class="editor-section-head">
                    <h3 class="editor-section-title">Parents</h3>
                </div>
                <div class="parents-grid">${grid}</div>
            </section>
        `;
    }

    function renderPermissionsSection(sub, scope) {
        const list = scope === 'global' ? sub.permissions : (sub.contexts[scope] || []);
        const rows = list.map((p, i) => renderPermRow(p, scope, i)).join('');

        return `
            <div class="perm-list" data-scope="${escapeHtml(scope)}">
                ${rows || '<p class="text-muted" style="font-size:0.85rem;margin:0 0 8px;">No permissions.</p>'}
                <button class="btn btn-sm btn-ghost" style="margin-top:6px;"
                        data-action="add-perm" data-scope="${escapeHtml(scope)}">+ Add permission</button>
            </div>
        `;
    }

    function renderPermRow(p, scope, index) {
        const cls = p.value ? '' : 'is-deny';
        const expires = p.expires > 0 ? p.expires : '';
        return `
            <div class="perm-row ${cls}">
                <input type="text" data-field="perm-node"
                       data-scope="${escapeHtml(scope)}" data-index="${index}"
                       value="${escapeHtml(p.node)}" placeholder="essentials.fly">
                <select data-field="perm-value"
                        data-scope="${escapeHtml(scope)}" data-index="${index}">
                    <option value="true" ${p.value ? 'selected' : ''}>grant</option>
                    <option value="false" ${!p.value ? 'selected' : ''}>deny</option>
                </select>
                <input type="text" data-field="perm-expires"
                       data-scope="${escapeHtml(scope)}" data-index="${index}"
                       value="${escapeHtml(expires)}" placeholder="expires (epoch ms)">
                <button class="icon-btn is-danger" title="Remove"
                        data-action="remove-perm"
                        data-scope="${escapeHtml(scope)}" data-index="${index}">×</button>
            </div>
        `;
    }

    function renderContextsSection(sub) {
        const names = Object.keys(sub.contexts);

        let blocks = '';
        if (names.length === 0) {
            blocks = '<p class="text-muted" style="font-size:0.85rem;margin:0;">No contexts.</p>';
        } else {
            for (const ctx of names) {
                blocks += `
                    <div class="context-item" data-context-block="${escapeHtml(ctx)}">
                        <div class="context-head">
                            <input type="text" class="context-name"
                                   data-field="context-name" data-old="${escapeHtml(ctx)}"
                                   value="${escapeHtml(ctx)}">
                            <button class="icon-btn is-danger" title="Remove context"
                                    data-action="remove-context" data-ctx="${escapeHtml(ctx)}">×</button>
                        </div>
                        ${renderPermissionsSection(sub, ctx)}
                    </div>
                `;
            }
        }

        return `
            <section class="editor-section">
                <div class="editor-section-head">
                    <h3 class="editor-section-title">Contexts</h3>
                    <button class="btn btn-sm btn-ghost" data-action="add-context">+ Add context</button>
                </div>
                ${blocks}
            </section>
        `;
    }

    function render() {
        renderFiles();
        renderWorkspace();
    }

    // FileReader keeps the UI responsive. Parsing happens in onload so a bad file does not freeze the page.
    function handleFile(kind, file) {
        const reader = new FileReader();
        reader.onload = e => {
            try {
                if (kind === 'groups') {
                    state.groups = loadGroupsYaml(e.target.result);
                    state.groupsLoaded = true;
                    if (!state.activeGroup || !state.groups[state.activeGroup]) {
                        state.activeGroup = Object.keys(state.groups)[0] || null;
                    }
                    state.activeTab = 'groups';
                } else {
                    state.users = loadUsersYaml(e.target.result);
                    state.usersLoaded = true;
                    if (!state.activeUser || !state.users[state.activeUser]) {
                        state.activeUser = Object.keys(state.users)[0] || null;
                    }
                    if (!state.groupsLoaded) state.activeTab = 'users';
                }
                render();
                toast('Loaded ' + file.name);
            } catch (err) {
                console.error(err);
                toast('Failed to parse ' + file.name + ': ' + err.message, true);
            }
        };
        reader.readAsText(file);
    }

    // Delegated listeners. The editor rebuilds innerHTML often, so direct listeners would get lost.
    function attachEvents() {
        document.addEventListener('click', onClick);
        document.addEventListener('input', onInput);
        document.addEventListener('change', onChange);
        document.addEventListener('change', e => {
            const input = e.target.closest('input[type="file"][data-input]');
            if (!input || !input.files || !input.files[0]) return;
            handleFile(input.dataset.input, input.files[0]);
            input.value = '';
        });

        document.addEventListener('dragover', e => {
            const card = e.target.closest('[data-drop]');
            if (!card) return;
            e.preventDefault();
            card.classList.add('is-dragover');
        });
        document.addEventListener('dragleave', e => {
            const card = e.target.closest('[data-drop]');
            if (!card) return;
            card.classList.remove('is-dragover');
        });
        document.addEventListener('drop', e => {
            const card = e.target.closest('[data-drop]');
            if (!card) return;
            e.preventDefault();
            card.classList.remove('is-dragover');
            const file = e.dataTransfer.files && e.dataTransfer.files[0];
            if (file) handleFile(card.dataset.drop, file);
        });
    }

    // Button actions routed through data-action.
    function onClick(e) {
        const btn = e.target.closest('[data-action]');
        if (!btn) return;
        const action = btn.dataset.action;

        switch (action) {
            case 'pick-groups':
            case 'pick-users': {
                const kind = action.split('-')[1];
                const input = document.querySelector(`input[data-input="${kind}"]`);
                if (input) input.click();
                return;
            }
            case 'download-groups': {
                if (!state.groupsLoaded) return;
                download('groups.yml', writeGroupsYaml(state.groups));
                toast('Downloaded groups.yml');
                return;
            }
            case 'download-users': {
                if (!state.usersLoaded) return;
                download('users.yml', writeUsersYaml(state.users));
                toast('Downloaded users.yml');
                return;
            }
            case 'clear-groups': {
                state.groups = {};
                state.groupsLoaded = false;
                state.activeGroup = null;
                if (state.activeTab === 'groups') state.activeTab = state.usersLoaded ? 'users' : 'groups';
                render();
                return;
            }
            case 'clear-users': {
                state.users = {};
                state.usersLoaded = false;
                state.activeUser = null;
                if (state.activeTab === 'users') state.activeTab = state.groupsLoaded ? 'groups' : 'users';
                render();
                return;
            }
            case 'tab': {
                state.activeTab = btn.dataset.tab;
                render();
                return;
            }
            case 'select-group': {
                state.activeGroup = btn.dataset.name;
                render();
                return;
            }
            case 'select-user': {
                state.activeUser = btn.dataset.key;
                render();
                return;
            }
            case 'new-group': {
                const name = prompt('New group name:');
                if (!name) return;
                if (!/^[^\s\/]{1,64}$/.test(name)) { toast('Invalid name.', true); return; }
                const key = name.toLowerCase();
                if (state.groups[key]) { toast('Group already exists.', true); return; }
                state.groups[key] = { options: { weight: '1000' }, parents: [], permissions: [], contexts: {} };
                state.activeGroup = key;
                state.activeTab = 'groups';
                render();
                return;
            }
            case 'new-user': {
                const name = prompt('New user name:');
                if (!name) return;
                if (!/^[^\s\/]{1,64}$/.test(name)) { toast('Invalid name.', true); return; }
                if (state.users[name]) { toast('User already exists.', true); return; }
                state.users[name] = {
                    key: name, uuid: null, name,
                    options: {}, parents: [], permissions: [], contexts: {},
                };
                state.activeUser = name;
                state.activeTab = 'users';
                render();
                return;
            }
            case 'delete-group': {
                const name = btn.dataset.name;
                const refs = Object.entries(state.groups)
                    .filter(([k, g]) => k !== name && g.parents.includes(name))
                    .map(([k]) => k);
                if (refs.length > 0) {
                    toast('Cannot delete: referenced by ' + refs.join(', '), true);
                    return;
                }
                if (!confirm(`Delete group "${name}"?`)) return;
                delete state.groups[name];
                state.activeGroup = Object.keys(state.groups)[0] || null;
                render();
                return;
            }
            case 'delete-user': {
                const key = btn.dataset.key;
                if (!confirm(`Delete user "${state.users[key].name || key}"?`)) return;
                delete state.users[key];
                state.activeUser = Object.keys(state.users)[0] || null;
                render();
                return;
            }
            case 'add-perm': {
                const scope = btn.dataset.scope;
                const sub = currentSubject();
                if (!sub) return;
                const list = scope === 'global' ? sub.permissions : (sub.contexts[scope] || []);
                list.push({ node: '', value: true, expires: 0 });
                render();

                requestAnimationFrame(() => {
                    const rows = document.querySelectorAll(`[data-field="perm-node"][data-scope="${CSS.escape(scope)}"]`);
                    const last = rows[rows.length - 1];
                    if (last) last.focus();
                });
                return;
            }
            case 'remove-perm': {
                const scope = btn.dataset.scope;
                const idx = parseInt(btn.dataset.index, 10);
                const sub = currentSubject();
                if (!sub) return;
                const list = scope === 'global' ? sub.permissions : (sub.contexts[scope] || []);
                list.splice(idx, 1);
                render();
                return;
            }
            case 'add-context': {
                const name = prompt('Context name (e.g. world:creative, region:spawn):');
                if (!name) return;
                const sub = currentSubject();
                if (!sub) return;
                if (sub.contexts[name]) { toast('Context already exists.', true); return; }
                sub.contexts[name] = [];
                render();
                return;
            }
            case 'remove-context': {
                const ctx = btn.dataset.ctx;
                const sub = currentSubject();
                if (!sub) return;
                if (!confirm(`Remove context "${ctx}"?`)) return;
                delete sub.contexts[ctx];
                render();
                return;
            }
            case 'add-option': {
                const sub = currentSubject();
                if (!sub) return;
                const key = prompt('Option key:');
                if (!key) return;
                if (sub.options[key] != null) { toast('Option already exists.', true); return; }
                sub.options[key] = '';
                render();
                return;
            }
            case 'remove-option': {
                const sub = currentSubject();
                if (!sub) return;
                delete sub.options[btn.dataset.key];
                render();
                return;
            }
        }
    }

    // Live edits for text/number inputs. These update state but do not re-render, so focus stays put.
    function onInput(e) {
        const t = e.target;
        const field = t.dataset.field;
        if (!field) return;
        const sub = currentSubject();
        if (!sub) return;

        if (field === 'weight') {
            sub.options.weight = t.value;
            return;
        }
        if (field === 'prefix') {
            sub.options.prefix = t.value;
            return;
        }
        if (field === 'user-name') {
            sub.name = t.value;
            return;
        }
        if (field === 'perm-node') {
            const list = t.dataset.scope === 'global'
                ? sub.permissions
                : (sub.contexts[t.dataset.scope] || []);
            const idx = parseInt(t.dataset.index, 10);
            if (list[idx]) list[idx].node = t.value;
            return;
        }
        if (field === 'perm-expires') {
            const list = t.dataset.scope === 'global'
                ? sub.permissions
                : (sub.contexts[t.dataset.scope] || []);
            const idx = parseInt(t.dataset.index, 10);
            if (list[idx]) {
                const n = parseInt(t.value.trim(), 10);
                list[idx].expires = Number.isFinite(n) && n > 0 ? n : 0;
            }
            return;
        }
        if (field === 'option-value') {
            sub.options[t.dataset.key] = t.value;
            return;
        }
    }

    // Changes that need a little more work, like parent checkboxes and context renames.
    function onChange(e) {
        const t = e.target;
        const field = t.dataset.field;
        if (!field) return;
        const sub = currentSubject();
        if (!sub) return;

        if (field === 'parent') {
            const name = t.value;
            if (t.checked) {
                if (!sub.parents.includes(name)) sub.parents.push(name);
            } else {
                sub.parents = sub.parents.filter(p => p !== name);
            }
            t.closest('.parent-chip').classList.toggle('is-checked', t.checked);
            return;
        }

        if (field === 'perm-value') {
            const list = t.dataset.scope === 'global'
                ? sub.permissions
                : (sub.contexts[t.dataset.scope] || []);
            const idx = parseInt(t.dataset.index, 10);
            if (list[idx]) {
                list[idx].value = t.value === 'true';
                t.closest('.perm-row').classList.toggle('is-deny', !list[idx].value);
            }
            return;
        }

        if (field === 'context-name') {
            const oldCtx = t.dataset.old;
            const newCtx = t.value.trim();
            if (newCtx === oldCtx) return;
            if (!newCtx) { t.value = oldCtx; return; }
            if (sub.contexts[newCtx]) { toast('Context already exists.', true); t.value = oldCtx; return; }

            const rebuilt = {};
            for (const [k, v] of Object.entries(sub.contexts)) {
                rebuilt[k === oldCtx ? newCtx : k] = v;
            }
            sub.contexts = rebuilt;
            render();
            return;
        }

        if (field === 'option-key') {
            const oldKey = t.dataset.old;
            const newKey = t.value.trim();
            if (newKey === oldKey) return;
            if (!newKey) { t.value = oldKey; return; }
            if (sub.options[newKey] != null) { toast('Option already exists.', true); t.value = oldKey; return; }
            const rebuilt = {};
            for (const [k, v] of Object.entries(sub.options)) {
                rebuilt[k === oldKey ? newKey : k] = v;
            }
            sub.options = rebuilt;
            render();
            return;
        }
    }


    // Inject toast element once, then check that js-yaml actually loaded.
    function init() {
        const el = document.createElement('div');
        el.id = 'editor-toast';
        el.className = 'editor-toast';
        document.body.appendChild(el);

        if (!window.jsyaml) {
            toast('js-yaml failed to load from CDN.', true);
            return;
        }

        render();
        attachEvents();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();