all:                                  \
	build/FinalReport.pdf         \
	build/Gantt.pdf               \
	build/ProjectCharter.pdf      \
	build/Slides.pdf              \
	build/Specifications.pdf      \
	build/TestPlan.pdf            \
	build/TechSlides.pdf

export TEXC = latexmk -output-directory=build -pdflatex="lualatex %O %S" -pdf -dvi- -ps-

.PHONY: FORCE

build/%_fromsvg.pdf: fig/%.svg
	inkscape $< --export-area-drawing --export-pdf=$@

build/ProjectCharter.pdf: ProjectCharter.tex titlepage.tex build/Gantt.pdf
	$(TEXC) -latexoption=-shell-escape $<

build/FinalReport.pdf: FinalReport.tex build/Gantt.pdf build/memory_layout_fromsvg.pdf build/micromachine_old_fromsvg.pdf build/micromachine_updated_fromsvg.pdf Actions/ActionManagement.pdf build/demo_load_fromsvg.pdf build/big_picture_fromsvg.pdf build/sequencer_old_fromsvg.pdf build/sequencer_updated_fromsvg.pdf
	$(TEXC) -latexoption=-shell-escape $<

build/%.pdf: %.tex FORCE
	$(TEXC) -latexoption=-shell-escape $<

Actions/ActionManagement.pdf: Actions/ActionManagement.ods
	libreoffice --headless --convert-to pdf --outdir Actions Actions/ActionManagement.ods 

clean: FORCE
	$(TEXC) -C

clean_all: FORCE
	rm -r build
