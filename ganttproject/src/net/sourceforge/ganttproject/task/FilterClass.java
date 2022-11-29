package net.sourceforge.ganttproject.task;

// Ideia: guardar várias variáveis, por exemplo, minDuration, maxDuration, etc
// E depois ter um metodo que verifique se uma tarefa satisfaz todos os filtros (que nao estejam a null)
public class FilterClass {
    private String restriction;

    public FilterClass(String restriction) {
        this.restriction = restriction;
    }


}
