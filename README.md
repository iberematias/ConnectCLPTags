# Software de Aquisição de Dados de Controlador Lógico Programável, Programmable Logic Controller.  
![](https://github.com/iberematias/ConnectCLPTags/blob/master/src/assets/logo.png)

A estrutura deste software é a base de muitos outros que uso para aquisição de dados, 
sejam para gerenciamento de energia ou de produção na industria 4.0.

Este software:
 1. Conecta no Banco de Dados e no CLP/PLC;
 2. Ler os TAGs do PLC cadastrados na tabela [tag]; 
 3. Insere as leituras no banco de dados. 

*******
Tecnologias    
 1. groovy 2.2.2 [groovy 2.5.11](https://groovy.apache.org/download.html)
 2. [PostgreSQL 8 ou superior](https://www.postgresql.org/download/)
 3. [Java lib. for Ethernet/IP (AllenBradley ControlLogix, Compact Logix PLCs)](https://github.com/EPICSTools/etherip)
 
 Conteudo
 1. App Back-end

*******

<div id='detalhes'/>  

## Detalhes da Aplicação  

Projeto desenvolvido em uma máquina Windows 10Pro, NetBeans IDE 8.2s.
Linha de produção cpm o PLC Compactlogix L33.
Minha base de dados tem 37 TAGs.
Na tabela [tag] deixei um campo datahora_status que utilizo para saber a última data hora de leitura daquela variável, contudo não foi implementado neste projeto. 
Utilizei esse projeto em 3 máquinas: Rapiberry PI 3 ARMv8 1GB. Windows Server 2012 e Windows 10 Pro i3 4GB. 
A tabela [grupo] foi criada apenas para organização das tags na tabela de medições.
Os campos version nas tabelas são para controle de versão das estruturas de dados, fique avontade para remover, alterar este e quaisquer outros.

*******

<div id='bancodedados'/>  

## BANCO DE DADOS  

Exemplo de dados.
vp = process variables (numeric(19,4)

Tabela [tag]

id | version | ativo | id | nome                    | datahora_status | tipo | descricao
--- | --- | --- | --- | --- | --- | --- | ---
37	| 0	      | true  |	1  | F_INV_GEN[0].REFFERENCE | null            | REAL |	REFERENCIA DE VELOCIDADE ESTEIRA DO FORNO

Tabela [medicao_vp]

id  | version | vp         | datahora                 | tag_real_id 
--- | --- | --- | --- | ---  
68	 |  0	   | 10526.0000	| 2018-11-30 11:23:53.373  | 4  
268 |	0	   | 15.6094	| 2018-11-30 11:32:57.701  | 18 

Esquema SQL

CREATE TABLE public.grupo <br /> 
( <br />
    id bigint NOT NULL, <br />
    version bigint NOT NULL, <br />
    descricao character varying(255) COLLATE pg_catalog."default" NOT NULL, <br />
    CONSTRAINT grupo_pkey PRIMARY KEY (id) <br />
) <br />
WITH ( <br />
    OIDS = FALSE <br />
) <br />
TABLESPACE pg_default; <br />


CREATE TABLE public.tag <br />
( <br />
    id bigint NOT NULL, <br />
    version bigint NOT NULL, <br />
    ativo boolean DEFAULT true, <br />
    grupo_id bigint, <br />
    nome character varying(255) COLLATE pg_catalog."default" NOT NULL, <br />
    datahora_status timestamp without time zone, <br />
    tipo character varying(50) COLLATE pg_catalog."default", <br />
    descricao character varying(255) COLLATE pg_catalog."default", <br />
    CONSTRAINT tag_pkey PRIMARY KEY (id), <br />
    CONSTRAINT fk3813200ab3211591 FOREIGN KEY (grupo_id) <br />
        REFERENCES public.grupo (id) MATCH SIMPLE <br />
        ON UPDATE NO ACTION <br />
        ON DELETE NO ACTION <br />
) <br />
WITH ( <br />
    OIDS = FALSE <br />
) <br />
TABLESPACE pg_default; <br />

CREATE TABLE public.medicao_vp <br />
( <br />
    id bigint NOT NULL, <br />
    version bigint NOT NULL, <br />
    vp numeric(19,4), <br />
    datahora timestamp without time zone NOT NULL, <br />
    tag_real_id bigint NOT NULL, <br />
    CONSTRAINT medicao_vp_pkey PRIMARY KEY (id), <br />
    CONSTRAINT medicao_vp_datahora_tag_real_id_key UNIQUE (datahora, tag_real_id), <br />
    CONSTRAINT fk38131a9478c23426vp FOREIGN KEY (tag_real_id) <br />
        REFERENCES public.tag (id) MATCH SIMPLE <br />
        ON UPDATE NO ACTION <br />
        ON DELETE NO ACTION <br />
) <br />
WITH ( <br />
    OIDS = FALSE <br />
) <br />
TABLESPACE pg_default; <br />
