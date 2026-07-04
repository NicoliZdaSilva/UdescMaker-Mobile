package com.udescmaker.api.taxonomy;

public enum CategoriaProjeto {
    ARDUINO("arduino", "Arduino"),
    AUTOMACAO("automacao", "Automação"),
    BRINQUEDOS("brinquedos", "Brinquedos"),
    COSTURA("costura", "Costura"),
    DECORACAO("decoracao", "Decoração"),
    EDUCACAO("educacao", "Educação"),
    ELETRONICA("eletronica", "Eletrônica"),
    IMPRESSAO_3D("impressao-3d", "Impressão 3D"),
    JARDINAGEM("jardinagem", "Jardinagem"),
    MARCENARIA("marcenaria", "Marcenaria"),
    PAPELARIA("papelaria", "Papelaria"),
    ROUPA("roupa", "Roupa"),
    SUSTENTABILIDADE("sustentabilidade", "Sustentabilidade");

    private final String id;
    private final String label;

    CategoriaProjeto(String id, String label){
        this.id = id;
        this.label = label;
    }

    public String getId(){
        return id;
    }

    public String getLabel(){
        return label;
    }
    public static CategoriaProjeto fromId(String id){
        for (CategoriaProjeto categoria : values()){
            if (categoria.getId().equalsIgnoreCase(id)){
                return categoria;
            }
        }
        throw new IllegalArgumentException(("Categoria desconhecida: "+ id));
    }
}