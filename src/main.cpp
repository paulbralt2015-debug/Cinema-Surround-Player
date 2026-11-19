
#include <QApplication>
#include "MainWindow.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);

    QApplication::setApplicationName("Cinema Surround Player");
    QApplication::setOrganizationName("CinemaSurround");

    MainWindow window;
    window.show();

    return app.exec();
}
