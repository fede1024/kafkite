let SessionLoad = 1
let s:so_save = &so | let s:siso_save = &siso | set so=0 siso=0
let v:this_session=expand("<sfile>:p")
silent only
cd ~/tmp/kafkite
if expand('%') == '' && !&modified && line('$') <= 1 && getline(1) == ''
  let s:wipebuf = bufnr('%')
endif
set shortmess=aoO
badd +105 src/kafkite/core.clj
badd +50 src/kafkite/listener.clj
badd +13 project.clj
badd +7 src/kafkite/kafka/partitioner.clj
argglobal
silent! argdel *
argadd src/kafkite/core.clj
edit src/kafkite/core.clj
set splitbelow splitright
set nosplitright
wincmd t
set winheight=1 winwidth=1
argglobal
setlocal fdm=indent
setlocal fde=0
setlocal fmr={{{,}}}
setlocal fdi=#
setlocal fdl=99
setlocal fml=1
setlocal fdn=20
setlocal fen
2
normal! zo
4
normal! zo
4
normal! zo
4
normal! zo
4
normal! zo
11
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
13
normal! zo
16
normal! zo
16
normal! zo
16
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
27
normal! zo
36
normal! zo
36
normal! zo
36
normal! zo
36
normal! zo
36
normal! zo
73
normal! zo
74
normal! zo
75
normal! zo
87
normal! zo
88
normal! zo
88
normal! zo
88
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
90
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
105
normal! zo
let s:l = 108 - ((52 * winheight(0) + 27) / 55)
if s:l < 1 | let s:l = 1 | endif
exe s:l
normal! zt
108
normal! 049|
tabnext 1
if exists('s:wipebuf')
  silent exe 'bwipe ' . s:wipebuf
endif
unlet! s:wipebuf
set winheight=1 winwidth=20 shortmess=filnxtToO
let s:sx = expand("<sfile>:p:r")."x.vim"
if file_readable(s:sx)
  exe "source " . fnameescape(s:sx)
endif
let &so = s:so_save | let &siso = s:siso_save
doautoall SessionLoadPost
let g:this_obsession = v:this_session
unlet SessionLoad
" vim: set ft=vim :
