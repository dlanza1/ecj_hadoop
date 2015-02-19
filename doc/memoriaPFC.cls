\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{memoriaPFC}[2007/04/23 v0.3.1.1 Plantilla para hacer el PFC de la Facultad de Ingenieria de la Universidad de Deusto.]
\DeclareOption{borrador}{%
  \PassOptionsToPackage{draft}{graphicx}
  % \PassOptionsToPackage{hyperref}{draft}
}
\ProcessOptions
\LoadClass[10pt,a4paper,oneside]{scrbook}
% \RequirePackage[automark,nouppercase]{scrpage2}
\RequirePackage[automark,markuppercase]{scrpage2}
\RequirePackage[utf8]{inputenc}
\RequirePackage[spanish,activeacute]{babel}
\RequirePackage{titlesec}
\RequirePackage{url}
\RequirePackage{fancybox}
\usepackage{float} 
% \RequirePackage{tabularx}
\RequirePackage[pdftex]{graphicx}
\RequirePackage[absolute]{textpos}
\RequirePackage{ifthen}
\RequirePackage[pdftex]{color}
\RequirePackage{fancyvrb}
\RequirePackage{amssymb}
\RequirePackage{multibib}
% \RequirePackage[hypertexnames=false,pdftex=true,colorlinks=true,linkcolor=blue,bookmarks=true]{hyperref}
% \RequirePackage{fancyhdr} % Tiene que ir después del paquete "geometry"
% \RequirePackage{amsfonts}
\RequirePackage{color}
% \RequirePackage{eurosym}
% \RequirePackage[style=long,cols=2,border=none,toc=true]{glossary}
% \RequirePackage{makeidx}
% \RequirePackage{supertabular}
\RequirePackage{listings}
% \setlength{\parindent}{0cm}

% Fuentes
% \newfont{\aufnttitle}{arial at 20pt}
% \newfont{\aufntsubtitle}{arial at 18pt}
% \newfont{\aufntnames}{arial at 12pt}

% Definiendo colores para los listados de código fuente
\definecolor{violet}{rgb}{0.5,0,0.5}
\definecolor{navy}{rgb}{0,0,0.5}

\definecolor{hellgelb}{rgb}{1,1,0.8}
\definecolor{colKeys}{rgb}{0,0,1}
\definecolor{colIdentifier}{rgb}{0,0,0}
\definecolor{colComments}{rgb}{1,0,0}
\definecolor{colString}{rgb}{0,0.5,0}

\lstset{
  float=hbp,
  basicstyle=\ttfamily\small,
  identifierstyle=\color{colIdentifier},
  keywordstyle=\color{colKeys},
  stringstyle=\color{colString},
  commentstyle=\color{colComments},
  columns=flexible,
  tabsize=4,
  frame=single,
  extendedchars=true,
  showspaces=false,
  showstringspaces=false,
  numbers=left,
  numberstyle=\tiny,
  breaklines=true,
  backgroundcolor=\color{hellgelb},
  breakautoindent=true,
  captionpos=b
}

%% 
%% INDICE y GLOSARIO
%% 

% \makeglossary
% \makeindex

% http://theoval.sys.uea.ac.uk/~nlct/latex/thesis/node25.html
% \glossaryname (por defecto "Glosario")
% \glossary{name=set,description=a collection of objects}

%% 
%% PORTADA
%% 

% Datos de la portada
% \newcommand{\autores}[4]{\def\@autorUno{#1}\def\@autorDos{#3}\def\@DNIUno{#2}\def\@DNIDos{#4}}
\newcommand{\autores}[2]{\def\@autorUno{#1}\def\@autorDos{#2}}
\newcommand{\titulacion}[1]{\def\@titulacion{#1}}
\newcommand{\especialidad}[1]{\def\@especialidad{#1}}
\newcommand{\areade}[1]{\def\@areade{#1}}
\newcommand{\departamentode}[1]{\def\@departamentode{#1}}
\newcommand{\director}[1]{\def\@director{Director: #1}}
\newcommand{\directora}[1]{\def\@directora{Directora: #1}}
\newcommand{\codirector}[1]{\def\@codirector{Codirector: #1}}
\newcommand{\codirectora}[1]{\def\@codirectora{Codirectora: #1}}
\newcommand{\prologo}[1]{\def\@prologo{#1}}
\newcommand{\agradecimientos}[1]{\def\@agradecimientos{#1}}
\newcommand{\descriptores}[1]{\def\@descriptores{#1}}

\AtBeginDocument{%
  \ifthenelse{\equal{\@autorDos}{}}{\def\@autores{\@autorUno}}{\def\@autores{\@autorUno{} y \@autorDos}}
  % \hypersetup{pdftitle={\@title},pdfauthor={\@autores}}
  % \hypersetup{pdfsubject={Proyecto Fin de Carrera},pdfkeywords={\@descriptores}}
  \pagestyle{CabeceraPFC}
}

% Para dibujar una box de un ancho y alto especifico
\newcommand\xyparbox[4][c]{\mbox{%
    \dimen0=2\fboxrule\advance\dimen0 2\fboxsep
    \dimen1=#2\advance\dimen1 -\dimen0
    \dimen2=#3\advance\dimen2 -\dimen0
    \parbox[#1][\dimen2][c]{\dimen1}{#4}}}

\newcommand{\insertarsuperiorportada}{
	\setlength{\unitlength}{1 cm}
	\thispagestyle{empty}
	\begin{picture}(18,4)
	\put(0,0){\includegraphics[width=4cm,height=4cm]{imagenes/cumlogo.jpg}}
	\put(10,0){\includegraphics[width=4cm,height=4cm]{imagenes/uexlogo.jpg}}
	\end{picture}
	\\[1.5cm]
	\begin{center}
	\textbf{{\huge UNIVERSIDAD DE EXTREMADURA}\\[1.0cm]
	{\LARGE CENTRO UNIVERSITARIO DE M\'ERIDA}}\\[1.0cm]
	{\Large INGENIERO EN INFORM\'ATICA EN TECNOLOG\'IAS DE LA INFORMACI\'ON}\\[1.0cm]
	{\Large PROYECTO FIN DE GRADO}\\[1cm]
	{\Huge \textbf {INTEGRACI\'ON DE UNA}}\\
	{\Huge \textbf {HERRAMIENTA DE C\'OMPUTO}}\\[0.25cm]
	{\Huge \textbf {EVOLUTIVO CON UNA DE}}\\[0.25cm]
	{\Huge \textbf {PROCESAMIENTO MASIVO DE}}\\
	{\Huge \textbf {INFORMACI\'ON}}\\
}

\newcommand{\insertarinferiorportada}{
	M\'erida - \@date{}
	\end{center}
}

\newcommand{\insertarautor}{
	{\large Autor: Daniel Lanza Garc\'ia}\\[0.5cm]
}

\newcommand{\hacerportada}{
	\insertarsuperiorportada
	\vspace{5cm}
	\insertarautor
	\insertarinferiorportada
}

\newcommand{\hacercontraportada}{
	\insertarsuperiorportada
	\vspace{3cm}
	\insertarautor
	{\large Director: Francisco Fern\'andez de Vega}\\[0.3cm]
	{\large Codirector: Francisco Ch\'avez de la O}\\[0.3cm]
	\vspace{0,5cm}
	\insertarinferiorportada
}


% %% 
%% CONTRAPORTADA
%% 

% Datos de la contraportada
\AtBeginDocument{%
  \ifthenelse{\equal{\@autorDos}{}}{\def\@autores{\@autorUno}}{\def\@autores{\@autorUno{} y \@autorDos}}
  % \hypersetup{pdftitle={\@title},pdfauthor={\@autores}}
  % \hypersetup{pdfsubject={Proyecto Fin de Carrera},pdfkeywords={\@descriptores}}
  \pagestyle{CabeceraPFC}
}

% Comando que dibuja la contraportada
% \newcommand{\hacercontraportada}{%
%   \begin{titlepage}%
%     
%     % Titulo (Pfc) y subtitulo (título del proyecto)
%     \begin{center}
%       \vspace{2.5cm}
%       \noindent\sffamily\Huge\bf\@title
%       \vspace{2.5cm}
%       
%       % Area, dpto., CUM, UEX
%       \ifthenelse{\equal{\@titulacion}{iti}} {\noindent\sffamily\huge Proyecto fin de carrera de Ingeniería Técnica en Informática. Especialidad \@especialidad.} {}
% 
%       \ifthenelse{\equal{\@titulacion}{it}} {\noindent\sffamily\huge Proyecto fin de carrera de Ingeniería Técnica en Telecomunicación. Especialidad \@especialidad.} {}
% 
%       \ifthenelse{\equal{\@titulacion}{idi}} {\noindent\sffamily\huge Proyecto fin de carrera de Ingeniería Técnica en Diseño Industrial.} {}
%       \vspace{2.0cm}
% 
%       \noindent\sffamily\Large Area de \@areade{}
%       \vspace{1mm}
% 
%       \noindent\sffamily\Large Departamento de \@departamentode{}
%       \vspace{1mm}
% 
%       \noindent\sffamily\Large Centro Universitario de Mérida
%       \vspace{1mm}
% 
%       \noindent\sffamily\Large Universidad de Extremadura
%     \end{center}
%     
%     % Logotipo de la Titulacion
%     \begin{textblock*}{15cm}(3cm,17cm)%%14.8cm
%       \includegraphics[width=15cm]{imgs/uex/\@titulacion.jpg}%
%     \end{textblock*}%
% 
%     % Autor, Director/a, codirector/a y fecha
%     \begin{textblock*}{11cm}(10cm,24cm)%
%       {\noindent\sffamily\Large\bf Mérida, \@date{}}\\
%       \vspace*{1em}\\%
%       {\noindent\sffamily\Large\bf\@autorUno{}}\\%
%       \ifthenelse{\equal{\@autorDos}{}} {} {\sffamily\Large\bf\@autorDos{}\\}
%       \ifthenelse{\equal{\@director}{Director: }} {} {\noindent\sffamily\Large\bf \@director{}\\}
%       \ifthenelse{\equal{\@directora}{Directora: }} {} {\noindent\sffamily\Large\bf \@directora{}\\}
%       \ifthenelse{\equal{\@codirector}{Codirector: }} {} {\noindent\sffamily\Large\bf \@codirector{}\\}
%       \ifthenelse{\equal{\@codirectora}{Codirectora: }} {} {\noindent\sffamily\Large\bf \@codirectora{}\\}
%     \end{textblock*}
%   \end{titlepage}
% 
%   % Pagina en blanco para notas
%   \cleardoublepage
%   \thispagestyle{empty}
%   \textcolor[rgb]{1.00,1.00,1.00}{palabra} % Pinta "palabra" de blanco
% }



\AtEndDocument{%
  % Aparecen todas las fuentes, no solo las que citemos explicitamente en el texto
  \nocite{*}
  \nociteotras{*}
}

%% 
%% Agradecimientos
%% 

% Comando que crea la página resumen
\newcommand{\haceragradecimientos}{%
  \cleardoublepage
  \subsection*{Agradecimientos}
  \@agradecimientos
  \thispagestyle{plain}
}

%% 
%% RESUMEN
%% 

% Comando que crea la página resumen
\newcommand{\hacerprologo}{%
  \cleardoublepage
  \subsection*{Pr\'ologo}
  \@prologo
  % \subsection*{Descriptores}
  % \@descriptores

  \thispagestyle{plain}
}

%% 
%% FORMATO DE LA PAGINA
%% 
% Necesitas que:
% - en la parte superior se deje (hasta el cuerpo) 3cm
% - la cabecera esté a 2cm de la parte superior [del cuerpo]
% Por tanto:
% 
% 3cm = 1 in + \voffset + \topmargin + \headheight + \headsep
% 2cm = \headsep
% 
% Traducimos en órdenes LaTeX.
% Respetamos los valores por defecto de \voffset y \headheight.
% Pon los valores adecuados si quieres cambiarlos:
% \setlength{\voffset}{XXX}
% \setlength{\headheight}{XXX}
% Asignamos valor a \headsep y deducimos el de \topmargin:
\setlength{\headsep}{2cm}
\setlength{\topmargin}{3cm}
\addtolength{\topmargin}{-1in}
\addtolength{\topmargin}{-\voffset}
\addtolength{\topmargin}{-\headheight}
\addtolength{\topmargin}{-\headsep}

% Necesitas que:
% - en la parte inferior se deje (desde el cuerpo) 3cm
% - el pie esté a 1cm de la parte inferior
% Por tanto, teniendo en cuenta el espacio dedicado a la cabecera y
% margen superior:
% 
% \paperheight = 3 cm + \textheight + 3cm
% 2 cm = \footskip
% 
% El valor de \paperheight se asigna mediante la opción a4paper de
% \documentclass o \LoadClass. Falta lo siguiente:
\setlength{\footskip}{2cm}
\setlength{\textheight}{\paperheight}
\addtolength{\textheight}{-6cm}

% Necesitas que:
% - en la parte izquierda del documento se deje 3.5cm
% - en la parte derecha del documento se deje 2.5cm
% Dado que es un documento a dos caras, se tiene:
% 3.5 cm = 1 in + \hoffset + \oddsidemargin
% 2.5 cm = 1 in + \hoffset + \evensidemargin
% \paperwith = 3.5 cm + \textwidth + 2.5 cm
% 
% Mantenemos el valor de \hoffset. Modifica la orden siguiente
% si quieres cambiarlo:
% \setlength{\hoffset}{XXX}
\setlength{\oddsidemargin}{3.5 cm}
\addtolength{\oddsidemargin}{-1in}
\addtolength{\oddsidemargin}{-\hoffset}
\setlength{\evensidemargin}{2.5 cm}
\addtolength{\evensidemargin}{-1in}
\addtolength{\evensidemargin}{-\hoffset}
\setlength{\textwidth}{\paperwidth}
\addtolength{\textwidth}{-6cm}

% Separacion de 3 milímetros entre los párrafos
\parskip=3mm
% Para compilar con pdfLaTeX:
\setlength{\pdfpagewidth}{\paperwidth}
\setlength{\pdfpageheight}{\paperheight}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 
% Esta es la tabla que aparece en la cabecera
\newcommand{\cabeceraimpar}{\textit{\headmark}}
\newcommand{\cabecerapar}{\textit{PROYECTO FIN DE CARRERA}}

% Creamos un estilo de página corporativo - UD
\newpagestyle{CabeceraPFC}
% Definicion de la cabecera:
{(0pt,0pt){\cabeceraimpar\hfill}{\hfill\cabecerapar}{\hfill\headmark\hfill}(0pt,0pt)}%
% Definición del pie de pagina:
{(0pt,0pt){\pagemark\hfill}{\hfill\pagemark}{\hfill\headmark\hfill}(0pt,0pt)}

% Sólo muestra secciones en el encabezado
% \automark[section]{section}
% Sólo muestra capítulos en el encabezado
% \automark[chapter]{section}
\automark[section]{chapter}

% Redefinimos el estilo de página "plain", que es el que aparece
% en las 'primeras páginas' de capitulos, etc. En principio, es
% exactamente igual que la anterior.
\renewpagestyle{plain}%
% Definicion de la cabecera:
{(0pt,0pt){\cabeceraimpar\hfill}{\hfill\cabecerapar}{\hfill\headmark\hfill}(0pt,0pt)}%
% Definición del pie de pagina:
{(0pt,0pt){\pagemark\hfill}{\hfill\pagemark}{\hfill\headmark\hfill}(0pt,0pt)}

% Por defecto, estilo de página corporativo
\pagestyle{CabeceraPFC}

% Esto es para que no aparezca el numero de página en las páginas en blanco.
\makeatletter
\def\cleardoublepage{\clearpage\if@twoside \ifodd\c@page\else
  \hbox{}
  \vspace*{\fill}
  \vspace{\fill}
  \thispagestyle{empty}
  \newpage
  \if@twocolumn\hbox{}\newpage\fi\fi\fi}
\makeatother



%% 
%% ENCABEZADOS DE SECCIONES
%% 

\renewcommand{\frontmatter}{%
  % Eliminamos cabeceras
  \pagestyle{empty}%
  \renewcommand{\cabecerapar}{}
  \pagenumbering{roman}%
}

\renewcommand{\mainmatter}{%
  \cleardoublepage
  \pagestyle{headings}
  % \newcommand{\cabeceraimpar}{\textit{\headmark}}
  \renewcommand{\cabecerapar}{\textit{PROYECTO FIN DE CARRERA}}
  \pagenumbering{arabic}

  % Redefinimos el formato de \chapter
  \titleformat{\chapter}[block]{\normalfont\sffamily\huge}{\bfseries\thechapter.}{.5em}{\filleft\bfseries\MakeUppercase}[\titlerule\vspace{2ex}]
  \titlespacing*{\chapter}{0pt}{*4}{*1}

  % Redefinimos el formato de \section
  \titleformat{\section}[block]{\normalfont\sffamily\Large}{\thesection}{.5em}{\bfseries}
  \titlespacing*{\section}{0pt}{*4}{*1}

  % Redefinimos el formato de \subsection
  \titleformat{\subsection}[block]{\normalfont\sffamily\large}{\thesubsection}{.5em}{\bfseries}
  \titlespacing*{\subsection}{0pt}{*4}{*1}

  % Redefinimos el formato de \subsubsection
  \titleformat{\subsubsection}[block]{\normalfont\sffamily\normalsize}{}{0pt}{$\,\blacksquare\;\;\;$\bfseries}
  \titlespacing*{\subsubsection}{0pt}{*4}{0.5ex}
}
% fin mainmatter

\renewcommand{\backmatter}{%
  % Redefinimos el comando \chapter para los capitulos de la memoria
  \titleformat{\chapter}[block]{\normalfont\sffamily\huge}{\bfseries\thechapter.}{.5em}{\filleft\bfseries\MakeUppercase}[\titlerule\vspace{2ex}]
  \titlespacing*{\chapter}{0pt}{*4}{*1}
}

%% 
%% COMANDOS
%% 

% Estas definiciones son para el comando \figura
\newlength{\anchoFigura}
\newlength{\anchoFloat}
\addtolength{\fboxsep}{2\fboxsep}
\renewcommand{\capfont}{\normalfont\normalcolor\sffamily\small}
\renewcommand{\caplabelfont}{\normalfont\normalcolor\sffamily\bfseries\small}

% El comando \figura nos permite insertar figuras comodamente, y utilizando
% siempre el mismo formato. Los parametros son:
% 1 -> Porcentaje del ancho de página que ocupará la figura (de 0 a 1)
% 2 --> Fichero de la imagen
% 3 --> Texto a pie de imagen
% 4 --> Etiqueta (label) para referencias
% 5 --> Opciones que queramos pasarle al \includegraphics
\newcommand{\figura}[5]{%
  \setlength{\anchoFloat}{#1\textwidth}%
  \addtolength{\anchoFloat}{-4\fboxsep}%
  \setlength{\anchoFigura}{\anchoFloat}%
  \begin{figure}[h]%
    \begin{center}%
      \Ovalbox{%
        \begin{minipage}{\anchoFloat}%
          \begin{center}%
            \includegraphics[width=\anchoFigura,#5]{#2}%
            \caption{#3}%
            \label{#4}%
          \end{center}%
        \end{minipage}
      }%
    \end{center}%
  \end{figure}%
}

% El comando \figuraSinMarco es igual que \figura, pero no pone marco a la figura.
% Viene bien para diagramas grandes.
\newcommand{\figuraSinMarco}[5]{%
  \begin{figure}%
    \begin{center}%
      \includegraphics[width=#1\textwidth,#5]{#2}%
      \caption{#3}%
      \label{#4}%
    \end{center}%
  \end{figure}%
}

% Referencia a una figura
\newcommand{\ver}[1]{(ver figura~\ref{#1})}

% Referencia a una sección, subsección, etc.
\newcommand{\verapartado}[1]{ver apartado~\ref{#1}}

% Para escribir extractos de codigo
% 
% Las tabulaciones se substituyen por dos espacios
\fvset{tabsize=2}
% Creamos un nuevo environment de fancyvrb para los ejemplos enmarcados
\DefineVerbatimEnvironment{VerbEj}{BVerbatim}{fontsize=\small,samepage=true,commandchars=\\\{\}}
% Colo de fondo
\definecolor{grisfondo}{gray}{0.9}
% Environment para extractos de codigo
\newenvironment{codigo}%
{\VerbatimEnvironment\begin{Sbox}\begin{VerbEj}}%
    {\end{VerbEj}\end{Sbox}\setlength{\fboxsep}{8pt}\begin{center}\fcolorbox{black}{grisfondo}{\TheSbox}\end{center}}

% Otro formato más bonito para código fuente
\newcommand{\codigofuente}[3]{%
  \lstinputlisting[language=#1,caption={#2}]{#3}%
}


% Para el preambulos de los capitulos
\newenvironment{precapitulo}%
{\begin{center}\begin{quote}\sffamily}%
    {\end{quote}\end{center}}


%% 
%% Miscelanea
%% 

% Esto es para que los primeros parrafos no esten indentados
% \makeatletter
% \let\@afterindenttrue\@afterindentfalse
% \@afterindentfalse
% \makeatother

% Definimos el nivel de detalle del índice: \section
\setcounter{tocdepth}{1}

% Redefinimos el nombre de las tablas.
% En babel está traducido por 'Cuadro' :-?
\addto\captionsspanish{\renewcommand{\tablename}{Tabla}}
\addto\captionsspanish{\renewcommand{\listtablename}{Índice de tablas}}


\newcites{otras}{Bibliografia (Otras fuentes)}

% Comandos para insertar comodamente las dos bibliografias de la memoria:
% la de libros y articulos, y la de 'otras fuentes'.
\newcommand{\bibliografia}[1]{%
  \cleardoublepage \addcontentsline{toc}{chapter}{Bibliografía}
  \renewcommand{\bibname}{BIBLIOGRAFÍA}
  % \renewcommand{\bibname}{Bibliografía (Libros y artículos)}
  % \renewcommand{\bibname}{BIBLIOGRAFÍA}
  % 
  % \cleardoublepage \addcontentsline{toc}{chapter}{Bibliografía}
  % \renewcommand{\bibname}{Bibliografía}

  % \bibliographystyle{aalphaurl}
  \bibliographystyle{plainurl}
  \bibliography{#1}
}

\newcommand{\bibliografiaOtras}[1]{%
 % \cleardoublepage \addcontentsline{toc}{chapter}{Bibliografía (Otras fuentes)}
  % \renewcommand{\bibname}{Bibliografía (Otras fuentes documentales)}
  %\renewcommand{\bibname}{BIBLIOGRAFÍA (OTRAS FUENTES)}
  %\bibliographystyleotras{plainurl}
  %\bibliographyotras{#1}
}


% Listados de código fuente
\renewcommand{\lstlistlistingname}{Índice de listados}
\renewcommand{\lstlistingname}{Listado}
