package net.videosc.interfaces.mappings_data_source;

public interface MappingsTableDataSource</*TFirstHeaderDataType, */TRowHeaderDataType, TColumnHeaderDataType, TItemDataType> {

    int getRowsCount();
    int getColumnsCount();
    void getMappings();

//    TFirstHeaderDataType getFirstHeaderData();
    TRowHeaderDataType getRowHeaderData(int index);
    TColumnHeaderDataType getColumnHeaderData(int index);
    TItemDataType getItemData(int rowIndex, int columnIndex);
}
