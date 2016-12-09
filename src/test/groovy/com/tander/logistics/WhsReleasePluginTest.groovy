package com.tander.logistics;

import com.tander.logistics.utils.PaludisPackage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by durov_an on 05.04.2016.
 */
public class WhsReleasePluginTest {
    @Test
    public void apply() throws Exception {

    }

    @Test
    public void paludis_test() throws Exception {
      //  PaludisPackage paludisPackage = new PaludisPackage("tander-tsdserver", "tomcatsrv-rc-tsd")


        // определить каталог с ebuild'ами

        // вытянуть лог по каталогу

        // если собираем релиз,
        // то вытягиваем сет предыдущего релиза, находим в нём номер релиза нашего подпроекта
        //

        // сперва поищем уже существующие коммиты по нашей задаче. Если они есть, то используем их


        //println paludisPackage.getBuildBySPPRTask("SP0799306").version
        //println paludisPackage.getBuildBySPPRTask("SP07784101").version
        //println paludisPackage.getBuildBySPPRTask(null).version

        // найти в логе последний коммит с номером задачи

        // если такой коммит есть, то берём номер из подходящего файла

        // вариант А, возвращаем новый номер

        // вариант Б, переименовываем старый файл, если есть
    }

}