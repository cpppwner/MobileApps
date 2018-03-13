package mobileapps.aau.at.ab02.listview;

/**
 * Special click listener invoked, when user clicks on a row.
 */
public interface RowOnClickListener {

    /**
     * Method called when user clicks on row.
     * @param rowText Text which is currently stored in the row.
     */
    void onClick(String rowText);
}
